# Set your SMTP email and App Password (e.g., from Gmail or SendGrid)
firebase functions:config:set smtp.user="your-org-email@gmail.com" smtp.password="your-app-password"

# Verify the config
firebase functions:config:get