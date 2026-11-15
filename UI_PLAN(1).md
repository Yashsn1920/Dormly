# Smart Hostel Complaint Management System: UI Plan

Instructions for the coding agent. This is the companion to `BACKEND_PLAN.md`. Read both fully, then work on **one UI stage at a time** when told which one.

---

## 0. How to work (read first)

1. Do **only** the stage you are told to do. Do not start later stages.
2. The backend is the source of truth. **Appendix A of `BACKEND_PLAN.md` (the route and view contract) must not change.** Every URL, form field name, request attribute and JSP path used here comes from it.
3. The UI stage replaces the plain placeholder JSPs the backend stages created. Same file paths, new markup.
4. **Do not edit** servlets, services, DAOs, models or filters. The one exception is the tiny EL helper class defined in UI Stage 0. If the UI needs data the contract does not provide, stop and report it instead of working around it.
5. Before writing any JSP, open the real model/report classes and use their actual getter names. This plan names properties from the backend plan (Section 8 lists them). If the code differs, **the code wins**; mention the difference in your report.
6. When a stage is finished, reply with: files created or changed, how to verify, and any deviation (ideally none).

---

## 1. Design brief

The reference is the earlier college-events site ("Campus Zero"): a neo-brutalist look with thick black outlines, hard offset shadows, a dotted background, flat bright colours and heavy type. This project keeps **the same structure, colours and component language**, but is an **official institutional tool**, not a student social site. So the energy is reduced, not removed.

| Keep (from the reference) | Change (more formal) |
|---|---|
| Light grey-blue canvas with a dot grid | Calmer: dots are fainter, no large coloured blocks |
| Thick black borders, **no rounded corners** | Slightly thinner borders (3px cards, 2px inner) |
| Hard offset shadow, no blur | Smaller offset (6px cards, 4px buttons) |
| Yellow / cyan / magenta / green palette | Colour is used **only for meaning** (status, role, actions), not decoration |
| Heavy display font + monospace labels | Sentence/Title Case headings; uppercase only for tiny labels and tags |
| Stat panel (dark box with coloured numbers) | Kept as the dashboard summary |
| Cards with a role tag and timestamp | Kept; tone of copy is formal |
| Tabs, banner, dashed "empty slot", corner "OFFICIAL" label | Kept |
| Header with black logo square, nav, accent button, role tag | Kept |
| Slang copy, emojis, rotated stickers | **Removed**. No emojis, no tilted tags, no playful copy |
| Student/Admin tab toggle on login | **Removed**: the role comes from the account, not a tab. A tab would mislead. (The tab component is reused for status filters.) |

Role colour coding, taken from the reference (student tag was yellow, admin tag and admin post border were cyan):
- **Student area:** accent colour **yellow**
- **Warden area:** accent colour **cyan**

---

## 2. Hard rules

**Technology (strictly Java ecosystem + plain web standards):**
- Views: JSP, JSTL 3.0 (`jakarta.tags.core`, `jakarta.tags.fmt`, `jakarta.tags.functions`), EL, and **JSP tag files** for reusable components.
- Styling: **hand-written CSS only**, split into a few files (Section 7). No Bootstrap, Tailwind, any CSS framework, preprocessor or npm build step.
- Scripting: **vanilla JavaScript only**, kept minimal (Section 5.14). **Every page must work with JavaScript disabled.** JS only enhances.
- No CDN or external requests of any kind. Fonts and icons are **self-hosted** (a college lab may have no internet).
- No images except the user's uploaded complaint photos. Icons are one inline SVG sprite.
- All user-supplied text is output with `<c:out>` (or `fn:escapeXml`), never raw `${...}` inside HTML.
- Semantic HTML (`header`, `nav`, `main`, `table` with `th scope`, `label for`, `button`, `dialog`).

**Design rules:**
- `border-radius: 0` everywhere.
- Shadows are always hard offsets (`Xpx Xpx 0 ink`), never blurred.
- Never use colour alone to carry meaning (always pair with text).
- Never put magenta **text** on a light background (contrast is only about 3.4:1). Magenta is used as a **background with black text**, or as text on black.
- No emojis, no exclamation marks in copy.

---

## 3. Design tokens

Put these in `tokens.css` as CSS custom properties.

### 3.1 Colour

| Token | Value | Use |
|---|---|---|
| `--ink` | `#0A0A0A` | Text, all borders, shadows, primary buttons |
| `--paper` | `#FBFBFA` | Card and input background |
| `--canvas` | `#EAEDF2` | Page background (with dot grid) |
| `--yellow` | `#FFF200` | Student accent, "Submitted" status, accent buttons |
| `--cyan` | `#00EEFF` | Warden accent, "In progress" status, info banners |
| `--magenta` | `#FF00CC` | Attention: destructive actions, "Reopened", errors, overdue stripe |
| `--green` | `#00FF41` | Success: "Resolved", approve/save buttons |
| `--grey-200` | `#D4D8DE` | Disabled fill, "Rejected" status |
| `--muted` | `#5B6270` | Secondary text (timestamps, hints). Contrast on canvas must stay at least 4.5:1 |
| `--placeholder` | `#8A90A0` | Placeholder text only |

`body.theme-student` sets `--accent: var(--yellow)`; `body.theme-warden` sets `--accent: var(--cyan)`. Components use `--accent`, never a hard-coded yellow/cyan, unless the meaning is fixed (e.g. status badges).

**Dot grid:** on `body`, `background-color: var(--canvas); background-image: radial-gradient(rgba(10,10,10,.28) 1px, transparent 1px); background-size: 26px 26px;`

### 3.2 Typography (self-hosted `woff2`, in `static/fonts/`)

| Role | Font | Fallback stack |
|---|---|---|
| Headings, stat numbers, wordmark | **Archivo Black** | `"Arial Black", Impact, sans-serif` |
| Body text, paragraphs, form values | **Space Grotesk** (400, 500, 700) | `system-ui, "Segoe UI", Arial, sans-serif` |
| Labels, tags, IDs, dates, table headers, buttons | **Space Mono** (400, 700) | `ui-monospace, Consolas, monospace` |

These are the closest free match to the reference. Download the files once into `static/fonts/` and declare them with `@font-face` (`font-display: swap`). If a font cannot be obtained, use the fallback stack and note it in the report.

Scale: h1 `2.25rem` (`1.75rem` on mobile), h2 `1.5rem`, h3 `1.125rem`, body `1rem`, label/tag `0.75rem`, data/mono `0.875rem`. Line height `1.5`; headings `1.15`. Heading letter-spacing `-0.01em`; labels `+0.08em`, uppercase.

### 3.3 Borders, shadows, spacing, motion

- Border: `--bw-card: 3px`, `--bw-inner: 2px`, always `solid var(--ink)`.
- Shadow: `--sh-card: 6px 6px 0 var(--ink)`, `--sh-btn: 4px 4px 0 var(--ink)`, `--sh-sm: 3px 3px 0 var(--ink)`.
- Spacing scale: `4, 8, 12, 16, 24, 32, 48, 64` px.
- Container: `max-width: 1120px`, side padding `24px` (`16px` under 640px). Forms: `720px`. Login card: `440px`.
- Motion: only on buttons, `transform` 80ms. Hover: `translate(2px,2px)`, shadow shrinks to `2px`. Active: `translate(4px,4px)`, shadow `0`. `@media (prefers-reduced-motion: reduce)` disables it.
- Focus: `:focus-visible { outline: 3px solid var(--ink); outline-offset: 2px; box-shadow: 0 0 0 6px var(--accent); }` on every interactive element.

---

## 4. Semantic mapping

### 4.1 Complaint status badge

Badge = small mono uppercase tag, `2px` ink border, text black unless stated.

| Status | Display | Fill |
|---|---|---|
| `SUBMITTED` | Submitted | yellow |
| `IN_PROGRESS` | In Progress | cyan |
| `RESOLVED` | Resolved | green |
| `CLOSED` | Closed | ink, **white** text |
| `REOPENED` | Reopened | magenta |
| `REJECTED` | Rejected | grey-200 |

Use the enum's display name for the label (`status.displayName`) and `status.name()` (lower-cased) for the CSS class, e.g. `badge--in_progress`.

### 4.2 Priority marker

Not colour-coded (colours are already used by status). Shown as filled and empty squares plus the word, in mono:
`HIGH` → `■■■ High`, `MEDIUM` → `■■□ Medium`, `LOW` → `■□□ Low`.

### 4.3 Overdue

- A black badge with **magenta text** and a clock icon: `OVERDUE`.
- In lists/tables, the card or row also gets a **6px magenta left border**.
- Only shown when the page has an `overdue` flag or `complaint.isOverdue(now)` is exposed; never recomputed in the JSP (see Section 8).

### 4.4 Role tag

Small tag next to the user's name in the header: `STUDENT` (yellow fill), `WARDEN` (cyan fill).

### 4.5 Flash message (`flash`)

A full-width bordered bar at the top of the content, icon on the left, black text:
`success` → green fill · `error` → magenta fill · `info` → cyan fill. Always has a text label ("Success", "Error", "Notice") before the message.

### 4.6 Buttons

| Variant | Look | Used for |
|---|---|---|
| `btn-primary` | Ink fill, white text, no shadow. Hover: accent fill, black text, shadow | Main action of a form (Sign In, Submit Complaint) |
| `btn-accent` | Accent fill | Header call-to-action (Raise a Complaint) |
| `btn-success` | Green fill | Approve, Confirm Resolution, Save Changes |
| `btn-danger` | Magenta fill | Reject, Deactivate, Delete, Reopen |
| `btn-secondary` | Paper fill | Reset, Cancel, View |
| `btn-sm` | Smaller padding | Table row actions |

---

## 5. Component specifications

Implement as CSS classes in `components.css`; repeated ones also as JSP tag files (Section 7).

1. **Page header (`pageTitle`)**: page title in Archivo Black, with an optional right-aligned action button. Under it a 3px ink rule. The **last word of the title** may sit on an ink block with accent-coloured text (the "STUDENT / DATABASE" device from the reference), used once per page at most.
2. **Card**: paper fill, 3px border, `--sh-card`, padding 24px. `card-header`: a 32px square icon box (accent fill, 2px border) + h2, then a 3px bottom rule. Variant `card--info`: cyan fill (the "Digital Backpack" panel), used for hints and side information.
3. **Stat panel (`statTile`)**: an ink box with a row of tiles; each tile has a large Archivo Black number in a colour (Total white, Pending yellow, In Progress cyan, Resolved green, Overdue magenta) and a tiny mono grey label underneath. Tiles separated by 2px lines in `#333`.
4. **Banner**: cyan fill, 3px border, icon + one line of text (as in the "verified announcements" banner). Used for page-level information.
5. **Form field (`field`)**: label (mono, uppercase, bold, 0.75rem) above; input with 3px border, paper fill; required marker = small magenta square after the label; hint line (muted, mono 0.75rem) below; error: input border stays ink but the field gets a **magenta chip** below with `!` and the message (black text). `select` uses `appearance:none` with an inline SVG chevron. `textarea` min-height 120px, vertical resize only. Focus: accent-coloured hard shadow.
6. **File input**: a bordered "Choose File" button plus the selected file name in mono; hint "JPG or PNG, maximum 2 MB".
7. **Rating input**: five square radio buttons (1 to 5) in a row, pure CSS using `input:checked + label`; checked squares fill accent. No JavaScript.
8. **Tabs / segmented control**: links in a row sharing borders (as the Student/Admin toggle in the reference). Active tab = accent fill. Used for status filters; each tab is a normal link with a query string.
9. **Tables** (warden): 3px outer border, header row ink with white mono uppercase text, body rows separated by 2px ink lines, cell padding `12px 16px`, hover row = faint accent tint. Wrapped in a div with `overflow-x:auto`.
10. **Complaint card** (student lists): card with ID in mono (`#0042`), category tag, title (h3), meta line (raised date · assigned to), status badge top-right (like the timestamp position in the feed cards), priority marker, optional overdue badge and left stripe, and a "View Details" secondary button.
11. **Timeline (`timeline`)**: vertical list. Each entry has a 14px square node (fill = status colour, 2px border) joined by a 3px ink line; right of it: status badge, date-time (mono, muted), "by {name}", and the remark in body text. Newest last, so it reads like a tracking page. The final entry's node is larger.
12. **Empty state (`emptyState`)**: dashed 2px ink border box (the "Empty slot" from the reference), muted mono text, optional action link.
13. **Corner label**: small ink tag with white mono text attached to the top-right corner of a card ("OFFICIAL" on notices).
14. **Confirm dialog + minimal JS (`app.js`, vanilla, under 80 lines):**
    - Forms with `data-confirm="Message"` open a styled native `<dialog>` (card style with square close button, like the reference modal) with "Cancel" and a confirm button; confirming submits the form. Without JS the form simply submits.
    - Show the chosen file name next to the file input.
    - Live character counter for textareas with `maxlength` (if present).
    - Nothing else. No fetch, no AJAX.
15. **Icons:** single SVG sprite `static/img/icons.svg`, 24px grid, `stroke-width: 2.5`, round-less caps (square) to match the borders. Needed: dashboard, plus, list, megaphone, user, users, chart, logout, check, x, alert, clock, image, search, printer. Used through `<svg><use href=".../icons.svg#name"/></svg>`.

---

## 6. Layout and structure

### 6.1 Application shell (all logged-in pages)

```
┌────────────────────────────────────────────────────────────────────┐
│ [■] HOSTEL COMPLAINTS   Dashboard  My Complaints  Notices   Name [STUDENT] [Log Out] │
├────────────────────────────────────────────────────────────────────┤  3px ink rule
│                                                                    │
│   (flash bar)                                                      │
│   Page title ................................ [Primary action]    │
│   ───────────────────────────────────────────────────────────      │
│   content in a 1120px container                                    │
│                                                                    │
├────────────────────────────────────────────────────────────────────┤
│ Hostel Complaint Management System · Mini Project · RVITM          │
└────────────────────────────────────────────────────────────────────┘
```

- **Header:** white (`paper`) with a 3px bottom border, sticky. Left: black 40px logo square (white "H" or icon) + wordmark `HOSTEL` in ink and `COMPLAINTS` in magenta (large bold text, so contrast is acceptable). The name lives in one place (the layout tag) so it is easy to change.
- **Student nav:** Dashboard · Raise Complaint (accent button, as the yellow button in the reference) · My Complaints · Notices · Profile.
- **Warden nav:** Dashboard · Complaints · Students · Notices · Reports · Profile.
- Right side: user name, role tag, and **Log Out** as a small black button inside a `POST /logout` form.
- Active nav item: accent fill and 2px border.
- **Under 720px:** the nav collapses into a `<details>` element ("Menu") with no JS.
- **Public pages** (login, register, error when logged out): header shows only the logo and wordmark.
- Footer: a single mono line, muted.

### 6.2 Grid

Content uses CSS grid: 12 columns desktop, gap 24px. Common arrangements: `8/4` (main + sidebar), `4/8` (profile), full width (tables). Below 900px everything stacks to one column. Stat tiles wrap 2 per row under 640px.

### 6.3 Responsive and print

- Desktop-first, but fully usable at **360px** width.
- `print.css`: hides header, footer, buttons, filters; removes shadows and dot grid; black text on white; used mainly for the Reports page and the complaint detail page.

---

## 7. File structure

```
src/main/webapp/
├─ static/
│   ├─ css/   tokens.css  base.css  components.css  layout.css  pages.css  print.css
│   ├─ js/    app.js
│   ├─ fonts/ (woff2 files)
│   ├─ img/   icons.svg  favicon.svg
│   └─ styleguide.html          (static page showing every component; dev reference)
└─ WEB-INF/
    ├─ tags/                    (JSP tag files)
    │   layout.tag  pageTitle.tag  flash.tag  icon.tag
    │   statusBadge.tag  priorityMarker.tag  overdueBadge.tag
    │   statTile.tag  emptyState.tag  timeline.tag  field.tag
    ├─ tlds/  hostel-functions.tld
    └─ views/ (existing structure from the backend plan; markup replaced)
src/main/java/com/hostel/util/ViewFunctions.java    (the only Java added by the UI stage)
src/test/java/com/hostel/util/ViewFunctionsTest.java
```

- `head` of the layout tag links the CSS files in this order: tokens, base, components, layout, pages; `print.css` with `media="print"`.
- Every page uses the layout tag:
  ```jsp
  <%@ taglib prefix="c"  uri="jakarta.tags.core" %>
  <%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
  <%@ taglib prefix="t"  tagdir="/WEB-INF/tags" %>
  <%@ taglib prefix="hc" uri="http://hostel.local/functions" %>
  <t:layout title="My Complaints" active="complaints"> ... </t:layout>
  ```
- The layout tag chooses the student or warden nav and the `theme-student`/`theme-warden` body class from `currentUser.role`, and the public variant when `currentUser` is null. It also renders the `flash` bar.

### EL helper functions (`ViewFunctions`, pure Java, JUnit-tested)

Needed because JSTL `fmt:formatDate` cannot format `java.time` values.

| Function | Result |
|---|---|
| `hc:dateTime(LocalDateTime)` | `15 Nov 2026, 3:09 pm` (null → `-`) |
| `hc:date(temporal)` | `15 Nov 2026` |
| `hc:hours(Double)` | `28.5 hrs`; 48 or more → `2 days 4 hrs`; null → `No data` |
| `hc:squares(Number value, int max)` | `■■■□□` rounded to nearest whole number; null → `-` |
| `hc:percent(long part, long whole)` | integer 0 to 100, `0` when whole is 0 |
| `hc:initial(String)` | first letter, upper case |
| `hc:maxMonthlyCount(List<MonthlyStat>)` | highest count in the list, minimum 1 |
| `hc:lowestRated(List<CategoryStat>)` | the `CategoryStat` with the lowest `avgRating` among entries with `ratingCount > 0`, or null |

These are presentation helpers only; no business rules.

---

## 8. Properties this plan assumes (verify against the code)

| Object | Properties used |
|---|---|
| `currentUser` | `name`, `role`, `email`, `phone`, `status`, `dashboardPath`; for a student also `usn`, `block`, `roomNo`, `roomLabel`, `createdAt` |
| `Complaint` | `id`, `student`, `category`, `title`, `description`, `priority`, `status`, `imagePath`, `assignedTo`, `createdAt`, `resolvedAt`, `dueAt`, `rating`, `feedback` |
| `Student` (as `complaint.student` and in `students`) | `name`, `usn`, `email`, `phone`, `block`, `roomNo`, `roomLabel`, `status`, `createdAt` |
| `ComplaintUpdate` | `status`, `remark`, `updatedByName`, `updatedAt` |
| `Notice` | `id`, `title`, `message`, `postedAt` (**no author name**, so the UI always shows the author as "Warden's Office") |
| Enums | `displayName` on `Category`, `Priority`, `ComplaintStatus`; use `.name()` for CSS classes and form values |
| `DashboardStats` | `total`, `pending`, `inProgress`, `resolved`, `rejected`, `overdue` |
| `CategoryStat` | `category`, `count`, `avgRating`, `ratingCount`, `avgResolutionHours` |
| `MonthlyStat` | `yearMonth`, `count` |
| `MonthlyReport` | `total` and a per-status count map (if the real class differs, adapt in the JSP) |
| `ComplaintFilter` | `status`, `category`, `priority`, `block`, `roomNo`, `fromDate`, `toDate`, `keyword` |
| `flash` | `type` (`success`, `error`, `info`) and `message` |

For the **overdue flag**: use the page-level `overdue` attribute on detail pages. In lists, use the complaint's own overdue method through an EL-callable getter if the model exposes one (for example `overdue`); if it does not, report it and do not recompute it in the JSP.

---

## 9. Stages

Each UI stage lists which backend stages must be finished, because the pages need real data.

### UI Stage 0: Foundation

**Needs:** Backend Stages 0 and 1.

**Build:**
- Fonts into `static/fonts/` with `@font-face`; `icons.svg` sprite; `favicon.svg`.
- The six CSS files, implementing Sections 3, 4, 5 and 6.
- `ViewFunctions` + `hostel-functions.tld` + JUnit tests (including null input, zero division, 0/47/48 hours, rating rounding).
- All tag files in Section 7. `layout.tag` must support both public and logged-in variants.
- `app.js` (Section 5.14).
- `static/styleguide.html`: a static page showing every token and component in every variant and state (buttons, fields with error, badges for all six statuses, priority markers, stat panel, tabs, table, card, timeline, empty state, banner, flash types, dialog).

**Done when:**
- `styleguide.html` opens straight from the file system or Tomcat and matches the tokens exactly.
- Browser network log shows **zero** external requests.
- Tests for `ViewFunctions` pass.
- Tab key reaches every interactive element in the styleguide with a visible focus style.

---

### UI Stage 1: Shell and authentication pages

**Needs:** Backend Stage 4; UI Stage 0.

**Build** (specs in Appendix A below):
- `auth/login.jsp`, `auth/register.jsp`, `error.jsp`, `access-denied.jsp`.
- `student/profile.jsp`, `warden/profile.jsp`.
- Wrap the placeholder dashboards (`student/dashboard.jsp`, `warden/dashboard.jsp`) in the layout tag so navigation works; their real content comes in later stages.

**Done when:**
- Login failure shows the `error` message and keeps the email; registration errors appear under each field and values are kept, except passwords.
- Registration success redirects to login with a green flash.
- Nav differs by role; the active item is highlighted; logout is a POST button.
- A student reaching a warden URL sees the styled access-denied page.
- Layout is usable at 360px and the mobile menu works with JS disabled.

---

### UI Stage 2: Student pages

**Needs:** Backend Stage 5; UI Stage 1.

**Build:** `student/dashboard.jsp`, `raise-complaint.jsp`, `my-complaints.jsp`, `complaint-detail.jsp`, `notices.jsp` (profile already done), per Appendix A.

**Done when:**
- Duplicate warning flow works visually: warning box, the existing complaint shown, the "Submit anyway" path, and the notice about re-selecting the image.
- Status tabs filter correctly and show the right active tab.
- Confirm, Reopen and Rating controls appear **only** when `canConfirm`, `canReopen`, `canRate` are true; reopening without a remark shows the error.
- Timeline matches the data; image appears only when present.
- A complaint title containing `<script>alert(1)</script>` is displayed as text.

---

### UI Stage 3: Warden pages

**Needs:** Backend Stage 6; UI Stage 2.

**Build:** `warden/dashboard.jsp`, `complaints.jsp`, `update-complaint.jsp`, `students.jsp`, `notices.jsp`, per Appendix A.

**Done when:**
- Dashboard tiles, category bars and overdue list show correct data; the pending-students banner appears only when the count is above zero.
- All filters are retained after submitting and "Reset" clears them.
- Only legal next statuses appear in the status control; the Reject form appears only when rejecting is allowed.
- Approve, Deactivate, Reactivate, Reject and Delete all ask for confirmation (with JS) and work without it.
- Tables scroll sideways inside their wrapper at 360px without breaking the page.

---

### UI Stage 4: Reports, print, accessibility, final polish

**Needs:** Backend Stage 7 (Stage 8 recommended); UI Stages 0 to 3.

**Build:**
- `warden/reports.jsp` per Appendix A.
- `print.css` tuned for Reports and complaint detail pages.
- Accessibility and consistency pass across **all** pages using the checklist in Section 10.
- Remove unused CSS and leftover placeholder markup. Keep `styleguide.html` as documentation.

**Done when:** every item in Section 10 passes.

---

## 10. Final QA checklist

1. **No-JS test:** disable JavaScript; every flow still works (confirm dialogs simply submit).
2. **Keyboard:** all pages operable by keyboard; focus is always visible; tab order follows the visual order.
3. **Contrast:** body text and badges meet 4.5:1; no magenta text on light backgrounds.
4. **Width:** check 360px, 768px, 1280px. No sideways page scroll (only table wrappers scroll).
5. **Long text:** a 150-character title and a very long unbroken word wrap without breaking cards (`overflow-wrap: anywhere`).
6. **Escaping:** `<script>`/HTML in titles, remarks, notices, names is shown as plain text.
7. **Empty states:** every list has one (no complaints, no notices, no students in a tab, no overdue, empty month).
8. **Print:** Reports and complaint detail print cleanly on A4, black on white.
9. **External requests:** none (fonts, icons, scripts all local).
10. **Consistency:** the same status always has the same badge; same action always the same button variant; copy follows Section 11.
11. **Contract:** every form field name and URL exactly matches Appendix A of `BACKEND_PLAN.md`.

---

## 11. Copy rules

- Formal, short, neutral. Sentences end with a full stop in messages; labels and buttons do not.
- Title Case for headings and buttons; uppercase only through CSS on tiny labels and tags.
- Address the user directly but without familiarity: "Your complaint has been submitted."
- Standard labels: Sign In, Register, Log Out, Submit Complaint, Submit Anyway, Confirm Resolution, Reopen Complaint, Submit Feedback, Save Changes, Change Password, Update Status, Reject Complaint, Assign, Approve, Deactivate, Reactivate, Post Notice, Delete, Apply Filters, Reset, Generate Report, Print.
- Empty states: "No complaints found.", "No notices have been posted.", "No students in this category.", "No overdue complaints.", "No data available for this month."
- Never expose raw enum names (`IN_PROGRESS`); always the display name.

---

## Appendix A: Page specifications

Routes, fields and attributes are those of Appendix A in `BACKEND_PLAN.md`. Below is what each page must **look like and contain**. `[ ]` = form control, `<c:out>` is assumed for all user text.

### A.1 `auth/login.jsp`

```
┌──────────────────────────────────────────────────────────────┐
│ [■] HOSTEL COMPLAINTS                                        │
├──────────────────────────────────────────────────────────────┤
│  Hostel Complaint            ┌─────────────────────────┐     │
│  ▌Management System▐         │ Sign In                 │     │
│                              │ EMAIL ADDRESS *         │     │
│  1  Register with your USN   │ [___________________]   │     │
│  2  Raise a complaint        │ PASSWORD *              │     │
│  3  Track it to resolution   │ [___________________]   │     │
│                              │ [      SIGN IN       ]  │     │
│                              │ New student? Register   │     │
│                              └─────────────────────────┘     │
└──────────────────────────────────────────────────────────────┘
```
- Left: h1 with "Management System" on the ink block device; three numbered short steps (square number boxes). Right: card, 440px, 6px shadow.
- `error` shown as a magenta banner inside the card; `email` re-filled; `flash` shown above the card.
- Under 900px: the left block collapses above the card, shortened.

### A.2 `auth/register.jsp`

- Single card (720px), header "Student Registration". Two-column grid: Full Name | USN, Email | Phone, Block | Room Number, Password | Confirm Password.
- Cyan info panel above the button: "Your account will be activated after approval by the warden."
- Password hint: "At least 8 characters, including a letter and a digit."
- Errors under each field; values re-filled from `form` (never passwords). Buttons: Register (primary), "Back to Sign In" (secondary).

### A.3 `student/dashboard.jsp`

```
Dashboard                                    [ + Raise a Complaint ]
Signed in as Yash · Room A-204
┌─ stat panel (ink) ─────────────────────────────────────────┐
│  12 Total │  3 Pending │  2 In Progress │  7 Resolved      │
└────────────────────────────────────────────────────────────┘
┌─ Recent Complaints ───────────────────┐ ┌─ Latest Notices ──┐
│ complaint card                        │ │ title · date      │
│ complaint card ... (up to 5)          │ │ title · date      │
│ [View All Complaints]                 │ │ [All Notices]     │
└───────────────────────────────────────┘ └───────────────────┘
```
- Data: `stats` (Total, Pending, In Progress, Resolved), `recent`, `latestNotices`. Grid 8/4.
- Empty states for both lists; the first-time empty state of complaints links to "Raise a Complaint".

### A.4 `student/raise-complaint.jsp`

- Card 720px, header "Raise a Complaint", `enctype="multipart/form-data"`, posts to `/student/complaints`.
- Order: read-only room box ("Room: A-204, taken from your profile") → Category (select from `categories`, display names) → Title (max 150, counter) → Description (textarea) → Image (optional file input with hint) → buttons: Submit Complaint (primary), Cancel (secondary, to My Complaints).
- Help panel (cyan, `card--info`): "Priority is assigned automatically from the category and cannot be changed." (Do not repeat the actual mapping in the UI.)
- **Duplicate state** (`duplicateWarning` present): a magenta-filled alert at the top of the form: "A similar complaint is already open for your room." Inside: a mini card of the existing complaint (`#id`, title, status badge, raised date, "View" link). Form values re-filled. A hidden field `confirmDuplicate=true` is rendered and the primary button label changes to **Submit Anyway**. Add the line: "If you attached an image, please select it again."

### A.5 `student/my-complaints.jsp`

- Page title "My Complaints". Tabs: All · Submitted · In Progress · Resolved · Closed · Reopened · Rejected (links with `?status=`; "All" has no parameter). Active tab from `selectedStatus`.
- List of complaint cards (Section 5.10). Count line: "Showing N complaints".
- Empty state per tab.

### A.6 `student/complaint-detail.jsp`

```
Complaint #42                                  [status badge]
┌─ main (8) ───────────────────────────┐ ┌─ sidebar (4) ───────┐
│ Title (h2)                           │ │ Details              │
│ Description                          │ │ Category  Plumbing   │
│ [image, 3px border, max 100% width]  │ │ Priority  ■■■ High   │
│ ── Progress ──                       │ │ Raised    15 Nov ... │
│ timeline                             │ │ Due       16 Nov ... │
│                                      │ │ Assigned  Plumber    │
│                                      │ │ [OVERDUE]            │
│                                      │ ├──────────────────────┤
│                                      │ │ Actions (conditional)│
└──────────────────────────────────────┘ └──────────────────────┘
```
- Actions card shown only if at least one flag is true:
  - `canConfirm`: green **Confirm Resolution** (POST `confirm`) with text "Confirm only if the issue is fixed."
  - `canReopen`: Remark textarea (required) + magenta **Reopen Complaint** (POST `reopen`).
  - `canRate`: "Rate the service" with the five-square rating (`rating`) + optional comment + **Submit Feedback**.
- If `rating` already exists: read-only "Your feedback: ■■■■□ 4 / 5" and the comment.
- "Due" is hidden for closed or rejected complaints. A Print button (secondary) uses `print.css`.

### A.7 `student/notices.jsp`

- Cyan banner: "Official notices from the Warden's Office."
- Notice cards: yellow square avatar with "W", author line "Warden's Office", posted date (mono, muted), corner label **OFFICIAL**, title (h3), message (preserve line breaks with CSS `white-space: pre-line`). Empty state.

### A.8 `student/profile.jsp` and `warden/profile.jsp`

- Layout 4/8 (as the "Student Database" reference): left identity card, right forms.
- **Left card:** large square avatar (accent fill, initial via `hc:initial`), name, role tag, then a definition list: Email, Phone, and for students USN, Room, Account status, Member since.
- **Right, card 1 "Personal Details":** Name, Phone (+ Block and Room Number for students). Email and USN displayed as locked read-only fields. Button: Save Changes (green).
- **Right, card 2 "Change Password":** Current Password, New Password, Confirm New Password; button Change Password (primary). Errors from `errors` shown per field.

### A.9 `warden/dashboard.jsp`

```
Dashboard                                         
[cyan banner: 3 students are awaiting approval.  Review →]   (only if count > 0)
┌ stat panel ─ 48 Total │ 9 Pending │ 12 In Progress │ 24 Resolved │ 4 Overdue ┐
┌─ Complaints by Category ─────────────┐ ┌─ Overdue Complaints ──────────┐
│ Plumbing/Water ██████████████░░ 12   │ │ table: ID, Title, Room,       │
│ Electrical     ████████░░░░░░░░  8   │ │ Priority, Due, [Open]         │
│ ...                                  │ │ (up to 10)                    │
└──────────────────────────────────────┘ └───────────────────────────────┘
```
- Category bars are **pure CSS**: a 2px-bordered track, cyan fill, width = `hc:percent(stat.count, stats.total)`, count at the right in mono.
- Overdue list empty state: "No overdue complaints."
- Overdue count tile number is magenta.

### A.10 `warden/complaints.jsp`

- **Filter card** (GET form to `/warden/complaints`), grid of 4 columns: Status, Category, Priority (selects with "All" option), Block, Room, From, To (date inputs), Keyword. Buttons: Apply Filters (primary), Reset (secondary link). Values re-filled from `filter`.
- Result count: "N complaints found".
- **Table** columns: ID · Student (name, room label) · Category · Title · Priority · Status (badge; + overdue badge) · Raised · Assigned To · [Open] (secondary `btn-sm`). Overdue rows get the magenta left border.
- Empty state: "No complaints found." with a Reset link.

### A.11 `warden/update-complaint.jsp`

- Header: "Complaint #42", status badge, overdue badge if `overdue`, and a magenta banner "This complaint is past its due date." when overdue.
- Layout 8/4.
- **Main:** title, description, image, "Student" info card (name, USN, room label, phone, email), timeline.
- **Sidebar "Actions":**
  - **Update Status:** select listing `allowedNextStatuses` **except** Rejected; Remark textarea; Update Status button. Hidden if no such status.
  - **Assign:** text input `assignedTo` with a `<datalist>` of suggestions (Electrician, Plumber, Housekeeping, Network Technician, Mess Supervisor, Security Staff); Assign button. Current assignee shown above.
  - **Reject:** shown only if Rejected is in `allowedNextStatuses`: Reason textarea (required) + magenta Reject Complaint button, with `data-confirm`.
  - If `allowedNextStatuses` is empty: an info note "No further actions are available for this complaint."
- Feedback (`rating`, `feedback`) shown read-only when present.

### A.12 `warden/students.jsp`

- Tabs: Pending · Active · Inactive (`?status=`; default Pending).
- Table: Name · USN · Email · Phone · Room · Registered · Actions. Actions are small POST forms with hidden `id`:
  - Pending → green **Approve**
  - Active → magenta **Deactivate** (`data-confirm`)
  - Inactive → green **Reactivate**
- Empty state per tab: "No students in this category."

### A.13 `warden/notices.jsp`

- Card 1 "Post a Notice": Title, Message, Post Notice (primary).
- Below: list of notice cards (same look as the student view) each with a magenta **Delete** button (`data-confirm`). Empty state.

### A.14 `warden/reports.jsp`

```
Reports                      Month [2026-11]  [Generate Report]  [Print]
┌ stat panel ─ 31 Complaints │ 1 day 6 hrs Avg resolution │ 4.1/5 Rating ┐
┌─ Complaints per Month (last 6) ──────┐ ┌─ By Status (this month) ─────┐
│ vertical CSS bars with month labels  │ │ badge + count rows            │
└──────────────────────────────────────┘ └───────────────────────────────┘
┌─ Category Performance ─────────────────────────────────────────────────┐
│ Category | Complaints | Avg Rating (■■■■□ 4.1) | Ratings | Avg Resolution │
│ ... the lowest-rated row has a magenta "LOWEST RATED" tag               │
└────────────────────────────────────────────────────────────────────────┘
```
- Month control: `<input type="month" name="month">` in a GET form, value from `selectedMonth`.
- Monthly bars: vertical, height = `hc:percent(stat.count, hc:maxMonthlyCount(monthlyCounts))`, month labels in mono; the selected month's bar uses the accent fill, others white.
- Averages use `hc:hours` and `hc:squares`; missing data reads "No data", never "0" or "NaN".
- "Lowest rated" uses `hc:lowestRated(categoryStats)`.
- Print version: no filters or buttons, header line with the month and the generation date.

### A.15 `error.jsp` and `access-denied.jsp`

- Centered card with an h1 ("Page Not Found", "Something Went Wrong", "Access Denied"), one explanatory sentence, and a button back to the right place (`currentUser.dashboardPath` if logged in, otherwise Sign In). Show the HTTP status code in mono if available. Never show stack traces or exception messages.

---

## Appendix B: Suggested order of work (backend and UI together)

| Step | Do | Result you can demo |
|---|---|---|
| 1 | Backend 0 → 1 → 2 → 3 | Data layer and rules, tested without a UI |
| 2 | UI 0 | Style guide page, all components |
| 3 | Backend 4, then UI 1 | Styled login, registration, profile, role-based navigation |
| 4 | Backend 5, then UI 2 | Complete student journey |
| 5 | Backend 6, then UI 3 | Complete warden journey |
| 6 | Backend 7, then UI 4 | Reports, print, final polish |
| 7 | Backend 8 | Demo data, hardening, README |

Backend stages never wait on UI stages; UI stages only wait on the backend stage they list.

---

## Appendix C: Prompt template for each stage

Paste this when you start a UI stage, changing the stage number:

> Read `BACKEND_PLAN.md` and `UI_PLAN.md` fully. Do **UI Stage N only**, exactly as written, obeying Section 2 (hard rules) and keeping Appendix A of `BACKEND_PLAN.md` untouched. Do not edit servlets, services, DAOs or models. Check the real getter names in the code before writing JSPs. Do not start any other stage. When finished, list the files you created or changed, how I can verify the "Done when" items, and any deviation or missing data you found. If something is impossible or ambiguous, stop and ask before improvising.
