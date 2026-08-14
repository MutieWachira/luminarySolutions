const {onCall, HttpsError, onRequest} = require("firebase-functions/v2/https");
const {onDocumentUpdated, onDocumentCreated, onDocumentDeleted} = require("firebase-functions/v2/firestore");
const {defineString, defineSecret} = require("firebase-functions/params");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const axios = require("axios");
const moment = require("moment");

admin.initializeApp();

// SECRETS
const smtpPassword = defineSecret("SMTP_PASSWORD");
const mpesaConsumerKey = defineSecret("MPESA_CONSUMER_KEY");
const mpesaConsumerSecret = defineSecret("MPESA_CONSUMER_SECRET");
const stripeSecretKey = defineSecret("STRIPE_SECRET_KEY");

// CONFIG
const senderEmail = defineString("SENDER_EMAIL", {default: "mutiewachira@gmail.com"});
const mpesaShortcode = defineString("MPESA_SHORTCODE", {default: "174379"}); // Default Sandbox
const mpesaEnv = defineString("MPESA_ENV", {default: "sandbox"}); // sandbox or production
const callbackUrlBase = defineString("CALLBACK_URL_BASE", {default: "https://us-central1-luminarysolutions-e0272.cloudfunctions.net"});

/**
 * M-Pesa Utility: Get Access Token
 */
async function getMpesaAccessToken() {
    let key = "";
    let secret = "";

    try {
        key = mpesaConsumerKey.value();
        secret = mpesaConsumerSecret.value();
    } catch (e) {
        console.warn("M-Pesa credentials not found in secrets. Checking environment variables.");
        // In some local dev environments, secrets might not be initialized properly
        key = process.env.MPESA_CONSUMER_KEY || "";
        secret = process.env.MPESA_CONSUMER_SECRET || "";
    }

    if (!key || !secret) {
        console.error("M-Pesa Consumer Key or Secret is missing.");
        throw new Error("M-Pesa configuration error: Missing credentials. Please set MPESA_CONSUMER_KEY and MPESA_CONSUMER_SECRET secrets.");
    }

    const auth = Buffer.from(`${key}:${secret}`).toString("base64");
    const url = mpesaEnv.value() === "production"
        ? "https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials"
        : "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";

    try {
        console.log(`Fetching M-Pesa access token from: ${url}`);
        const response = await axios.get(url, {
            headers: { Authorization: `Basic ${auth}` }
        });
        return response.data.access_token;
    } catch (error) {
        console.error("M-Pesa Auth Error:", error.response?.data || error.message);
        throw new Error("Failed to authenticate with M-Pesa. Check your consumer key and secret.");
    }
}

/**
 * CALLABLE: Initiate STK Push
 * Triggers the M-Pesa PIN prompt on the user's phone.
 */
exports.initiateStkPush = onCall({
    secrets: [mpesaConsumerKey, mpesaConsumerSecret],
    enforceAppCheck: false
}, async (request) => {
    const { amount, phoneNumber, userId, reference } = request.data;

    if (!amount || !phoneNumber) {
        throw new HttpsError("invalid-argument", "Amount and Phone Number are required.");
    }

    // Format phone number to 254XXXXXXXXX (12 digits)
    let formattedPhone = phoneNumber.replace(/\D/g, "");
    if (formattedPhone.startsWith("0")) {
        formattedPhone = "254" + formattedPhone.substring(1);
    } else if (formattedPhone.startsWith("+")) {
        formattedPhone = formattedPhone.substring(1);
    }

    if (!formattedPhone.startsWith("254")) {
        formattedPhone = "254" + formattedPhone;
    }

    if (formattedPhone.length !== 12) {
        throw new HttpsError("invalid-argument", "Invalid phone number format. Expected 2547XXXXXXXX.");
    }

    const timestamp = moment().format("YYYYMMDDHHmmss");
    const shortcode = mpesaShortcode.value();

    let passkey = "";
    if (mpesaEnv.value() === "sandbox") {
        console.log("Using default Safaricom Sandbox passkey.");
        passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    }

    if (!passkey) {
        console.error("M-Pesa Passkey is missing. Cannot generate password.");
        throw new HttpsError("failed-precondition", "M-Pesa Passkey is not configured. Production requires a secret passkey.");
    }

    const password = Buffer.from(`${shortcode}${passkey}${timestamp}`).toString("base64");

    const accessToken = await getMpesaAccessToken();
    const baseUrl = mpesaEnv.value() === "production"
        ? "https://api.safaricom.co.ke"
        : "https://sandbox.safaricom.co.ke";

    const url = `${baseUrl}/mpesa/stkpush/v1/processrequest`;

    // Point to our mpesaCallback HTTP function.
    let callbackUrl = callbackUrlBase.value();
    if (!callbackUrl.includes("a.run.app") && !callbackUrl.endsWith("mpesaCallback")) {
        callbackUrl = `${callbackUrl}/mpesaCallback`;
    }

    // Safaricom Limits: AccountReference (12 chars), TransactionDesc (20 chars)
    const safeReference = (reference || "Donation").substring(0, 12).trim();
    const safeDesc = "Luminary Payment".substring(0, 20);

    const payload = {
        BusinessShortCode: shortcode,
        Password: password,
        Timestamp: timestamp,
        TransactionType: mpesaEnv.value() === "production" ? "CustomerPayBillOnline" : "CustomerPayBillOnline",
        Amount: Math.round(amount),
        PartyA: formattedPhone,
        PartyB: shortcode,
        PhoneNumber: formattedPhone,
        CallBackURL: callbackUrl,
        AccountReference: safeReference,
        TransactionDesc: safeDesc
    };

    try {
        console.log(`[STK Push] Initiating for ${formattedPhone}, Amount: ${amount}, Ref: ${safeReference}`);
        // Do not log the full payload to avoid leaking the base64 password in logs
        const response = await axios.post(url, payload, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });

        const resData = response.data;
        console.log("[STK Push] Safaricom Response:", JSON.stringify(resData));

        if (resData.ResponseCode !== "0") {
            throw new Error(resData.ResponseDescription || "Safaricom rejected the request");
        }

        const transactionId = resData.CheckoutRequestID;

        // Store transaction attempt in Firestore for tracking
        await admin.firestore().collection("payments").doc(transactionId).set({
            userId: userId || "anonymous",
            amount: amount,
            phoneNumber: formattedPhone,
            status: "Pending",
            checkoutRequestId: transactionId,
            merchantRequestId: resData.MerchantRequestID,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            reference: safeReference
        });

        return {
            success: true,
            checkoutRequestId: transactionId,
            customerMessage: resData.CustomerMessage || "Request accepted for processing"
        };
    } catch (error) {
        const errorData = error.response?.data || error.message;
        console.error("STK Push Error:", JSON.stringify(errorData));
        throw new HttpsError("internal", error.response?.data?.errorMessage || error.message || "STK Push initiation failed");
    }
});

/**
 * CALLABLE: Query STK Push Status
 * Manually check the status of a transaction if the callback is delayed.
 */
exports.queryStkStatus = onCall({
    secrets: [mpesaConsumerKey, mpesaConsumerSecret],
    enforceAppCheck: false
}, async (request) => {
    const { checkoutRequestId } = request.data;

    if (!checkoutRequestId) {
        throw new HttpsError("invalid-argument", "CheckoutRequestID is required.");
    }

    const timestamp = moment().format("YYYYMMDDHHmmss");
    const shortcode = mpesaShortcode.value();

    let passkey = "";
    if (mpesaEnv.value() === "sandbox") {
        passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    }

    if (!passkey) {
        throw new HttpsError("failed-precondition", "M-Pesa Passkey is not configured.");
    }

    const password = Buffer.from(`${shortcode}${passkey}${timestamp}`).toString("base64");

    const accessToken = await getMpesaAccessToken();
    const baseUrl = mpesaEnv.value() === "production"
        ? "https://api.safaricom.co.ke"
        : "https://sandbox.safaricom.co.ke";

    const url = `${baseUrl}/mpesa/stkpushquery/v1/query`;

    const payload = {
        BusinessShortCode: shortcode,
        Password: password,
        Timestamp: timestamp,
        CheckoutRequestID: checkoutRequestId
    };

    try {
        const response = await axios.post(url, payload, {
            headers: { Authorization: `Bearer ${accessToken}` }
        });

        const resData = response.data;
        console.log("M-Pesa Query Response:", JSON.stringify(resData));

        if (resData.ResultCode === "0") {
            // Update Firestore if we found a success that wasn't recorded yet
            await admin.firestore().collection("payments").doc(checkoutRequestId).update({
                status: "Completed",
                resultDesc: resData.ResultDesc,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }

        return resData;
    } catch (error) {
        console.error("STK Query Error:", error.response?.data || error.message);
        throw new HttpsError("internal", error.response?.data?.errorMessage || "Failed to query status");
    }
});

/**
 * CALLABLE: Process Card Payment (Production Ready)
 * Uses PaymentIntents for better security and 3D Secure support.
 */
exports.processCardPayment = onCall({
    secrets: [stripeSecretKey],
    enforceAppCheck: false
}, async (request) => {
    const { amount, paymentMethodId, userId, reference } = request.data;

    if (!amount || !paymentMethodId) {
        throw new HttpsError("invalid-argument", "Amount and PaymentMethod ID are required.");
    }

    console.log(`Processing PaymentIntent for user ${userId}, amount ${amount}`);

    try {
        const stripe = require("stripe")(stripeSecretKey.value());

        // 1. Create and Confirm the PaymentIntent
        // We set confirm: true to process it immediately if no extra auth (like 3DS) is needed
        const intent = await stripe.paymentIntents.create({
            amount: Math.round(amount * 100), // Stripe expects cents
            currency: "kes",
            payment_method: paymentMethodId,
            confirm: true,
            description: reference || "Luminary Solutions Donation",
            automatic_payment_methods: {
                enabled: true,
                allow_redirects: "never"
            },
            metadata: {
                userId: userId || "anonymous",
                reference: reference || "none"
            }
        });

        if (intent.status === "succeeded") {
            const transactionId = intent.id;

            // 2. Record in Firestore
            await admin.firestore().collection("payments").doc(transactionId).set({
                userId: userId || "anonymous",
                amount: amount,
                method: "Card",
                status: "Completed",
                stripeIntentId: transactionId,
                reference: reference || "Card Donation",
                createdAt: admin.firestore.FieldValue.serverTimestamp()
            });

            return {
                success: true,
                transactionId,
                message: "Payment processed successfully"
            };
        } else {
            console.warn(`PaymentIntent status: ${intent.status}`);
            return {
                success: false,
                status: intent.status,
                message: `Payment status: ${intent.status}. Further action may be required.`
            };
        }

    } catch (error) {
        console.error("Stripe Payment Error:", error);
        throw new HttpsError("internal", error.message || "Card payment failed");
    }
});

/**
 * HTTP: M-Pesa Callback
 * Safaricom calls this URL after the user enters their PIN.
 * NOTE: Ensure this function is public in Google Cloud Console (allUsers -> Cloud Functions Invoker)
 */
exports.mpesaCallback = onRequest({
    cors: true,
    maxInstances: 10
}, async (req, res) => {
    try {
        console.log("M-Pesa Callback Raw Body:", JSON.stringify(req.body));

        const callbackData = req.body?.Body?.stkCallback;
        if (!callbackData) {
            console.error("Invalid Callback Body Structure");
            return res.status(400).send("Invalid Request");
        }

        console.log("M-Pesa Callback Received:", JSON.stringify(callbackData));

        const checkoutRequestId = callbackData.CheckoutRequestID;
        const resultCode = callbackData.ResultCode;
        const resultDesc = callbackData.ResultDesc;

        const paymentRef = admin.firestore().collection("payments").doc(checkoutRequestId);

        if (resultCode === 0) {
            // Success
            const metadata = callbackData.CallbackMetadata?.Item;
            if (!metadata) {
                console.error("Missing CallbackMetadata for successful transaction");
                await paymentRef.update({ status: "Failed", resultDesc: "Missing metadata from Safaricom" });
                return res.status(200).send("Callback Processed with error");
            }

            const getMetaValue = (name) => metadata.find(i => i.Name === name)?.Value;

            const mpesaReceiptNumber = getMetaValue("MpesaReceiptNumber");
            const amount = getMetaValue("Amount");
            const transactionDate = getMetaValue("TransactionDate");
            const phoneNumber = getMetaValue("PhoneNumber");

            await paymentRef.update({
                status: "Completed",
                mpesaReceiptNumber: mpesaReceiptNumber,
                amount: amount, // Sync actual amount from Safaricom
                mpesaPhoneNumber: phoneNumber,
                transactionDate: transactionDate,
                completedAt: admin.firestore.FieldValue.serverTimestamp(),
                rawCallbackData: callbackData
            });

            console.log(`[Callback] Payment successful: ${mpesaReceiptNumber} for ${amount} from ${phoneNumber}`);
        } else {
            // Failed/Cancelled (e.g., Code 1032 for Cancelled)
            console.log(`Payment failed/cancelled. Code: ${resultCode}, Desc: ${resultDesc}`);
            await paymentRef.update({
                status: "Failed",
                resultCode: resultCode,
                resultDesc: resultDesc,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
        }

        return res.status(200).send("Callback Processed");
    } catch (error) {
        console.error("Error processing M-Pesa callback:", error);
        return res.status(500).send("Internal Server Error");
    }
});

/**
 * Core Email Sending Logic
 */
async function sendStatusEmail(email, name, status, password = null) {
    const user = senderEmail.value();
    const pass = smtpPassword.value();

    console.log(`[DEBUG] Initializing sendStatusEmail for: ${email}`);
    console.log(`[DEBUG] SENDER_EMAIL: ${user}`);

    if (!pass) {
        const errorMsg = "CRITICAL: SMTP_PASSWORD secret is NOT SET. Check Firebase Secrets.";
        console.error(`[ERROR] ${errorMsg}`);
        throw new Error(errorMsg);
    }

    console.log(`[DEBUG] SMTP_PASSWORD length: ${pass.length}`);

    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: user,
            pass: pass
        },
        // Security hardening for certain environments
        tls: {
            rejectUnauthorized: false
        }
    });

    const safeName = name || "Valued Volunteer";

    let subject = "";
    let html = "";

    const footer = `
        <div style="margin-top: 30px; border-top: 1px solid #ddd; padding-top: 20px; color: #777; font-size: 0.85em;">
            <p><strong>Luminary Solutions Management</strong><br>
            <em>Advancing Technology for Social Impact</em></p>
            <p>This is an automated message. Please do not reply directly to this email. For support, please contact help@luminarysolutions.com</p>
        </div>
    `;

    if (status === "Approved" || status === "Approve") {
        subject = "Official Approval: Your Volunteer Application with Luminary Solutions";
        html = `
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; line-height: 1.6; max-width: 600px; border: 1px solid #eee; padding: 20px; border-radius: 10px;">
                <h2 style="color: #10B981; border-bottom: 2px solid #10B981; padding-bottom: 10px;">Volunteer Application Approved</h2>
                <p>Dear ${safeName},</p>
                <p>On behalf of <strong>Luminary Solutions</strong>, I am pleased to inform you that your application to join our volunteer network has been formally reviewed and <strong>approved</strong>.</p>
                <p>We are impressed with your background and are eager to see the positive impact you will bring to our initiatives. To facilitate your integration, we have provisioned a professional volunteer account for you.</p>

                <div style="background-color: #f4fbf9; padding: 20px; border-radius: 8px; border-left: 5px solid #10B981; margin: 20px 0;">
                    <p style="margin: 0; font-weight: bold; color: #065f46;">Access Credentials:</p>
                    <p style="margin: 10px 0 5px 0;"><strong>Platform:</strong> Luminary Solutions Mobile Application</p>
                    <p style="margin: 5px 0;"><strong>Username:</strong> ${email}</p>
                    <p style="margin: 5px 0;"><strong>Temporary Password:</strong> <code style="background: #fff; padding: 2px 5px; border: 1px solid #ddd;">${password}</code></p>
                </div>

                <p><strong>Next Steps:</strong></p>
                <ol>
                    <li>Download the Luminary Solutions app from the provided link.</li>
                    <li>Log in using the credentials above.</li>
                    <li>Update your profile and complete the mandatory orientation module.</li>
                </ol>

                <p style="font-style: italic; font-size: 0.9em; color: #666;">Note: For security reasons, you will be prompted to change your password upon your first successful login.</p>

                <p>Welcome to the mission. We look forward to working together.</p>
                ${footer}
            </div>
        `;
    } else if (status === "TeamWelcome") {
        subject = "Official Onboarding: Welcome to the Luminary Solutions Team";
        html = `
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; line-height: 1.6; max-width: 600px; border: 1px solid #eee; padding: 20px; border-radius: 10px;">
                <h2 style="color: #6366F1; border-bottom: 2px solid #6366F1; padding-bottom: 10px;">Welcome to the Team</h2>
                <p>Dear ${safeName},</p>
                <p>It is my pleasure to welcome you to the <strong>Luminary Solutions</strong> professional team. We are confident that your expertise will be a significant asset to our organization.</p>
                <p>Your official staff credentials have been generated and are ready for use:</p>

                <div style="background-color: #f5f6ff; padding: 20px; border-radius: 8px; border-left: 5px solid #6366F1; margin: 20px 0;">
                    <p style="margin: 0; font-weight: bold; color: #3730a3;">Staff Access Details:</p>
                    <p style="margin: 10px 0 5px 0;"><strong>System:</strong> Luminary Solutions Internal Portal</p>
                    <p style="margin: 5px 0;"><strong>Username:</strong> ${email}</p>
                    <p style="margin: 5px 0;"><strong>Temporary Password:</strong> <code style="background: #fff; padding: 2px 5px; border: 1px solid #ddd;">${password}</code></p>
                </div>

                <p>Please ensure you log in to the Luminary Solutions internal dashboard within the next 24 hours to review your onboarding checklist and assigned projects.</p>

                <p>We are delighted to have you with us and wish you a productive career at Luminary Solutions.</p>
                ${footer}
            </div>
        `;
    } else if (status === "Rejected") {
        subject = "Decision Regarding Your Volunteer Application - Luminary Solutions";
        html = `
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; line-height: 1.6; max-width: 600px; border: 1px solid #eee; padding: 20px; border-radius: 10px;">
                <h2 style="color: #EF4444; border-bottom: 2px solid #EF4444; padding-bottom: 10px;">Application Status Update</h2>
                <p>Dear ${safeName},</p>
                <p>Thank you for your interest in volunteering with <strong>Luminary Solutions</strong> and for taking the time to submit your application and share your motivations with us.</p>
                <p>We have completed our review process for the current intake period. After careful consideration of all applicants and our specific operational needs at this time, we regret to inform you that we are unable to accept your application for this cycle.</p>
                <p>Please be assured that this decision is based solely on our current resource alignment and does not reflect a lack of merit in your profile. We will retain your application in our talent pool for the next six months and may contact you should a suitable opportunity arise.</p>
                <p>We appreciate your commitment to social impact and wish you the very best in your future pursuits.</p>
                ${footer}
            </div>
        `;
    } else if (status === "AccountDeleted") {
        subject = "Formal Confirmation of Account Closure - Luminary Solutions";
        html = `
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; line-height: 1.6; max-width: 600px; border: 1px solid #eee; padding: 20px; border-radius: 10px;">
                <p>Dear ${safeName},</p>
                <p>This email provides formal confirmation that your account and all associated personal data have been permanently removed from the <strong>Luminary Solutions</strong> active directory, effective immediately.</p>
                <p>In accordance with our data privacy policies, your information has been purged from our primary systems. If this deletion was unintentional or if you require further assistance, please contact our administrative desk.</p>
                <p>Thank you for your previous association with Luminary Solutions.</p>
                ${footer}
            </div>
        `;
    } else {
        console.warn(`No formal template for status: ${status}`);
        return;
    }

    try {
        console.log(`Attempting to send ${status} email to ${email}...`);
        const info = await transporter.sendMail({
            from: `"Luminary Solutions Admin" <${user}>`,
            to: email,
            subject: subject,
            html: html
        });
        console.log(`Email successfully delivered to ${email}. MessageId: ${info.messageId}`);
    } catch (error) {
        console.error(`ERROR: Failed to deliver email to ${email}:`, error.message);
        throw error;
    }
}

/**
 * TRIGGER: Team Member Creation.
 * Orchestrates Auth account creation, User profile creation, and Email delivery.
 */
exports.onTeamMemberCreated = onDocumentCreated({
    document: "luminary/teams/items/{memberId}",
    secrets: [smtpPassword]
}, async (event) => {
    const newValue = event.data?.data();
    if (!newValue) return;

    const { email, name } = newValue;
    const memberId = event.params.memberId;
    if (!email) return;

    console.log(`Setting up team member ${memberId} (${email})...`);

    try {
        const firstName = (name || "Member").split(" ")[0];
        let password = firstName;
        if (password.length < 6) {
            password = firstName.padEnd(6, '123');
        }

        // 1. Create Auth User (using the Firestore ID as UID)
        try {
            await admin.auth().createUser({
                uid: memberId,
                email: email,
                password: password,
                displayName: name,
            });
            console.log(`Auth user created for team member ${email}`);
        } catch (e) {
            if (e.code === 'auth/email-already-exists' || e.code === 'auth/uid-already-exists') {
                console.log(`Team member ${email} already exists in Auth.`);
            } else {
                throw e;
            }
        }

        // 2. Set Role
        await admin.auth().setCustomUserClaims(memberId, { role: "TEAM" });

        // 3. Create/Sync User Document in "users" collection
        await admin.firestore().collection("users").doc(memberId).set({
            id: memberId,
            name: name,
            email: email,
            role: "TEAM",
            enabled: true,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
        }, { merge: true });

        // 4. Send Welcome Email
        await sendStatusEmail(email, name, "TeamWelcome", password);
        console.log(`Full setup completed for team member ${memberId}`);

    } catch (error) {
        console.error(`ERROR: Failed to set up team member ${memberId}:`, error);
    }
});

/**
 * TRIGGER: Team Member Deletion.
 */
exports.onTeamMemberDeleted = onDocumentDeleted({
    document: "luminary/teams/items/{memberId}",
    secrets: [smtpPassword]
}, async (event) => {
    const oldValue = event.data?.data();
    if (!oldValue) return;

    const memberId = event.params.memberId;
    const { email, name } = oldValue;

    try {
        console.log(`Deleting Auth user and user profile for ${memberId}...`);

        // 1. Delete from Auth
        try {
            await admin.auth().deleteUser(memberId);
        } catch (authError) {
            if (authError.code !== 'auth/user-not-found') console.error(authError);
        }

        // 2. Delete User Profile
        await admin.firestore().collection("users").doc(memberId).delete();

        // 3. Send Deletion Email
        if (email) {
            await sendStatusEmail(email, name, "AccountDeleted");
        }

    } catch (error) {
        console.error(`Error in onTeamMemberDeleted for ${memberId}:`, error);
    }
});

/**
 * TRIGGER: Volunteer status change.
 * Handles account creation when status becomes "Approved".
 */
exports.onVolunteerStatusChange = onDocumentUpdated({
    document: "lumisphere/volunteers/items/{volunteerId}",
    secrets: [smtpPassword]
}, async (event) => {
    const newValue = event.data?.after.data();
    const previousValue = event.data?.before.data();

    // Safety check: Only proceed if status has actually changed
    if (!newValue || newValue.status === previousValue?.status) {
        console.log("No status change detected for volunteer, skipping email trigger.");
        return;
    }

    const { email, name, status } = newValue;
    const volunteerId = event.params.volunteerId;

    if (!email) {
        console.error(`ERROR: Volunteer ${volunteerId} has no email address.`);
        return;
    }

    console.log(`Volunteer ${volunteerId} status changed from '${previousValue?.status}' to '${status}'.`);

    try {
        if (status === "Approved") {
            const firstName = (name || "Volunteer").split(" ")[0];
            let password = (newValue.password) ? newValue.password : firstName;
            if (password.length < 6) {
                password = password.padEnd(6, '123');
            }

            console.log(`Approving volunteer ${volunteerId}: Ensuring Auth and User Profile...`);

            let actualUid = volunteerId;

            // 1. Create Auth User or Fetch existing
            try {
                const userRecord = await admin.auth().createUser({
                    uid: volunteerId,
                    email: email,
                    password: password,
                    displayName: name
                });
                console.log(`Auth user created with UID: ${userRecord.uid}`);
            } catch (e) {
                if (e.code === 'auth/email-already-exists') {
                    const existingUser = await admin.auth().getUserByEmail(email);
                    actualUid = existingUser.uid;
                    console.log(`User ${email} already exists with UID: ${actualUid}. Updating this record instead.`);
                } else if (e.code === 'auth/uid-already-exists') {
                    console.log(`UID ${volunteerId} already exists. Proceeding with this UID.`);
                } else {
                    console.error("Critical error creating Auth user:", e);
                    throw e;
                }
            }

            // Set/Update Role Claim using the confirmed UID
            await admin.auth().setCustomUserClaims(actualUid, { role: "VOLUNTEER" });

            // 2. Create/Sync User Profile in "users" collection
            // We use actualUid to ensure the profile matches the Auth record
            await admin.firestore().collection("users").doc(actualUid).set({
                id: actualUid,
                volunteerDocId: volunteerId, // Link back to the original application
                name: name,
                email: email,
                role: "VOLUNTEER",
                enabled: true,
                updatedAt: admin.firestore.FieldValue.serverTimestamp()
            }, { merge: true });

            console.log(`User profile synced for UID: ${actualUid}`);

            // NEW: Automatically assign to projects linked during signup
            const projectIds = newValue.projectIds || (newValue.projectId ? [newValue.projectId] : []);
            if (projectIds.length > 0) {
                console.log(`Assigning volunteer to projects: ${projectIds.join(", ")}`);
                const batch = admin.firestore().batch();
                projectIds.forEach(pid => {
                    const projectRef = admin.firestore().collection("lumisphere").doc("projects").collection("items").doc(pid);
                    batch.update(projectRef, {
                        volunteers: admin.firestore.FieldValue.arrayUnion(actualUid)
                    });
                });
                await batch.commit();
            }

            // 3. Send Approval Email
            await sendStatusEmail(email, name, "Approved", password);
        } else if (status === "Rejected") {
            console.log(`Volunteer ${volunteerId} rejected. Sending notification...`);
            await sendStatusEmail(email, name, "Rejected");
        } else {
            console.log(`Status changed to '${status}', no email template defined for this specific transition.`);
        }
    } catch (error) {
        console.error(`CRITICAL: Failed to process status change for volunteer ${volunteerId}:`, error);
    }
});

/**
 * TRIGGER: Volunteer Deletion.
 * Cleanup Auth account, user profile, and send confirmation email.
 */
exports.onVolunteerDeleted = onDocumentDeleted({
    document: "lumisphere/volunteers/items/{volunteerId}",
    secrets: [smtpPassword]
}, async (event) => {
    const oldValue = event.data?.data();
    if (!oldValue) return;

    const volunteerId = event.params.volunteerId;
    const { email, name } = oldValue;

    try {
        console.log(`Deleting Auth user and user profile for volunteer ${volunteerId}...`);

        // 1. Delete from Auth
        // Note: The volunteer document ID is usually the Auth UID in this system
        try {
            await admin.auth().deleteUser(volunteerId);
            console.log(`Auth account for ${volunteerId} deleted.`);
        } catch (authError) {
            if (authError.code === 'auth/user-not-found') {
                console.log(`Auth account for ${volunteerId} not found, already deleted.`);
            } else {
                console.error(`Error deleting Auth user ${volunteerId}:`, authError);
            }
        }

        // 2. Delete User Profile from 'users' collection
        await admin.firestore().collection("users").doc(volunteerId).delete();
        console.log(`User profile for ${volunteerId} deleted.`);

        // 3. Send Deletion Email
        if (email) {
            await sendStatusEmail(email, name, "AccountDeleted");
        }

    } catch (error) {
        console.error(`Error in onVolunteerDeleted for ${volunteerId}:`, error);
    }
});

/**
 * CALLABLE FUNCTIONS (Manual tools)
 */

exports.testSmtpConnection = onCall({
    secrets: [smtpPassword],
    enforceAppCheck: false
}, async (request) => {
    // V2 functions data is in request.data, but handles empty calls too
    const user = senderEmail.value();
    const pass = smtpPassword.value();

    console.log(`[TEST] Starting SMTP test for user: ${user}`);

    if (!pass) {
        const msg = "SMTP_PASSWORD secret is NOT SET in the current environment.";
        console.error(`[TEST] ${msg}`);
        throw new HttpsError('failed-precondition', msg);
    }

    const transporter = nodemailer.createTransport({
        service: 'gmail',
        auth: { user, pass },
        tls: { rejectUnauthorized: false }
    });

    try {
        console.log(`[TEST] Verifying transporter...`);
        await transporter.verify();
        console.log(`[TEST] Verification SUCCESSFUL.`);
        return {
            success: true,
            message: `Connected successfully as ${user}. Mail system is active.`,
            debug: { user, passSet: true, passLength: pass.length }
        };
    } catch (error) {
        console.error("[TEST] SMTP Verification FAILED:", error.message);
        throw new HttpsError('internal', `SMTP Connection Failed: ${error.message}. TIP: If using Gmail, you MUST use an 'App Password'.`);
    }
});

exports.sendVolunteerStatusEmail = onCall({
    secrets: [smtpPassword],
    enforceAppCheck: false
}, async (request) => {
    const { email, name, status, password } = request.data;
    try {
        await sendStatusEmail(email, name, status, password);
        return { success: true };
    } catch (error) {
        throw new HttpsError('internal', error.message);
    }
});

/**
 * CALLABLE: Test M-Pesa Authentication
 * Use this to verify your Consumer Key and Secret are correctly configured.
 */
exports.testMpesaAuth = onCall({
    secrets: [mpesaConsumerKey, mpesaConsumerSecret],
    enforceAppCheck: false
}, async (request) => {
    try {
        console.log("[TEST] Testing M-Pesa authentication...");
        const token = await getMpesaAccessToken();
        console.log("[TEST] M-Pesa token fetched successfully.");
        return {
            success: true,
            message: "M-Pesa Authentication Successful!",
            env: mpesaEnv.value()
        };
    } catch (error) {
        console.error("[TEST] M-Pesa Auth Test Failed:", error.message);
        throw new HttpsError('internal', `M-Pesa Auth Failed: ${error.message}`);
    }
});
