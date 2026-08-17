# Redesign Client Dashboard for Production Readiness

Redesign the client dashboard to provide a professional, high-performance experience with clear MVVM architecture, personalized content, and improved scalability.

## User Review Required

> [!IMPORTANT]
> The redesign introduces a new `UiState` structure using a sealed interface, which improves state management but requires updating the screen to handle all states (Loading, Success, Error).

> [!NOTE]
> We will add a "Quick Actions" section which initially contains placeholders for "Invoices" and "Support" as these features might not be fully implemented in the backend yet.

## Proposed Changes

### Data & Models

#### [NEW] [DashboardStats.kt](file:///home/sherlskyy/AndroidStudioProjects/luminarySolutions/app/src/main/java/com/example/luminarysolutions/data/models/DashboardStats.kt)
Create a model to encapsulate dashboard-specific metrics like active projects, pending requests, and total investments.

---

### Repository Layer

#### [MODIFY] [ClientRepository.kt](file:///home/sherlskyy/AndroidStudioProjects/luminarySolutions/app/src/main/java/com/example/luminarysolutions/data/repository/ClientRepository.kt)
Add methods to fetch:
- Projects specific to the logged-in client.
- Client-specific notifications or alerts.

---

### ViewModel Layer

#### [MODIFY] [ClientDashboardViewModel.kt](file:///home/sherlskyy/AndroidStudioProjects/luminarySolutions/app/src/main/java/com/example/luminarysolutions/ui/client/ClientDashboardViewModel.kt)
- Refactor `UiState` to use a `sealed interface`.
- Inject `AuthRepository` to identify the current user.
- Combine flows from `UserRepository` and `ClientRepository` to build a comprehensive dashboard state.

---

### UI Layer

#### [MODIFY] [ClientDashboardScreen.kt](file:///home/sherlskyy/AndroidStudioProjects/luminarySolutions/app/src/main/java/com/example/luminarysolutions/ui/client/ClientDashboardScreen.kt)
- **Header**: Add personalized greeting with user's name and profile picture.
- **Stats**: Modernized cards with gradient backgrounds or distinct icons.
- **Quick Actions**: A grid of buttons for common tasks (Request Service, View Invoices, Support).
- **Ongoing Projects**: A horizontal carousel showing active project progress.
- **Featured Services**: A "New Opportunities" section.
- **Error/Empty States**: User-friendly messaging and retry logic.

---

## Verification Plan

### Automated Tests
- Unit tests for `ClientDashboardViewModel` to verify state transitions based on repository data.
- UI test (if applicable) to ensure all sections render correctly in `Success` state.

### Manual Verification
- Deploy to device/emulator.
- Verify user profile data loads correctly.
- Check if project counts and lists match the underlying data.
- Verify navigation from "Quick Actions" and "Featured Services".
