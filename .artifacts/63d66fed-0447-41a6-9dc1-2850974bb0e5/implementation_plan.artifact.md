# Redesign Client Profile Page

Redesign the client profile page to be production-ready, professional, and feature-rich. The new design will follow MVVM architecture and implement common user requirements like profile editing, preference management, and security toggles.

## User Review Required

> [!IMPORTANT]
> The redesign will introduce new UI components and interaction patterns. Ensure the color palette and typography align with the overall Luminary branding.

## Proposed Changes

### UI Layer

#### [MODIFY] [ClientProfileScreen.kt](file:///home/sherlskyy/AndroidStudioProjects/luminarySolutions/app/src/main/java/com/example/luminarysolutions/ui/client/ClientProfileScreen.kt)
- Redesign the profile header with a modern, professional look.
- Implement sections for "Account", "Preferences", "Support", and "Legal".
- Add interactive toggles for Notifications, Dark Mode, and Two-Factor Authentication.
- Add an "Edit Profile" dialog to allow users to update their personal information (Name, Phone, Bio).
- Improve spacing, typography, and visual hierarchy.

### ViewModel Layer

#### [MODIFY] [ClientProfileViewModel.kt](file:///home/sherlskyy/AndroidStudioProjects/luminarySolutions/app/src/main/java/com/example/luminarysolutions/ui/client/ClientProfileViewModel.kt)
- Add functions to update user preferences (Notifications, Dark Mode, 2FA).
- Add functionality to update user profile information.
- Enhance the UI state to handle update loading and error states.

### Data Layer

#### [MODIFY] [UserRepository.kt](file:///home/sherlskyy/AndroidStudioProjects/luminarySolutions/app/src/main/java/com/example/luminarysolutions/data/repository/UserRepository.kt)
- (Optional) Ensure `updateUserProfile` is robust and handles all `User` fields correctly. (Already seems quite good).

## Verification Plan

### Automated Tests
- N/A (Manual verification on device/emulator is preferred for UI redesign).

### Manual Verification
1.  Navigate to the Client Profile screen.
2.  Verify the new professional design and layout.
3.  Toggle "Notifications", "Dark Mode", and "2FA" and verify they persist (if backend supports it).
4.  Open "Edit Profile", change name/phone/bio, and save. Verify changes are reflected.
5.  Perform a logout and verify redirection to the Public Dashboard.
