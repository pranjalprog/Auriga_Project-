Claude :

Claude finished the response
Diagnosing a submission error on a hosted app.
Diagnosing a submission error on a hosted app.
Ah, yeh classic Codespaces issue hai — samajh gaya. Aapka browser GitHub ke domain (cautious-space-trout-...app.github.dev) se page load kar raha hai, lekin humara code http://localhost:8080 ko call kar raha hai — browser ka "localhost" wahi machine hai jahan browser chal raha hai (aapka laptop), Codespace nahi. Isliye request kahin pahunch hi nahi rahi.

Fix: Vite ka proxy feature use karenge — frontend /api calls ko apne hi domain (5173) se bhejega, aur Vite server internally (Codespace ke andar) unhe localhost:8080 tak forward kar dega. Yeh sabse clean solution hai, CORS ki zaroorat bhi khatam ho jayegi.

Step 1: vite.config.js update karo
frontend/vite.config.js kholo, poora content replace karo:

javascript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
Step 2: src/api.js ko relative path pe badlo
javascript
import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export default api;
(Bas http://localhost:8080/api ki jagah sirf /api kar diya)

Step 3: Frontend restart karo (config change hai, HMR isse pick nahi karega)
Frontend wale terminal mein Ctrl+C, fir:

bash
cd /workspaces/Auriga_Project-/frontend
npm run dev
Step 4: Browser tab refresh karo
Wahi tab (frontend wala) refresh karo, aur dobara Book Appointment try karo — Doctor ID 1, Patient ID 1, koi bhi future date-time.

Is baar CORS/localhost issue nahi aayega. Result batao — agar phir bhi error aaye, browser ka DevTools Console (F12) khol ke exact error paste kar dena, jaldi pakड़ लेंगे।

1789643108927_image.png
"Booked! Appointment ID: 5" 🎉 — proxy fix kaam kar gaya, booking end-to-end chal rahi hai (React → Vite proxy → Spring Boot → Postgres).

Ab baaki do tabs test karo:

Test 1: Doctor Schedule tab
"Doctor Schedule" pe click karo
Doctor ID 1 daalo → "View Schedule"
Expected: list mein appointments dikhein (including abhi ka id 5 wala), har ek ke saamne "Cancel" button
Test 2: Overlap test — UI se bhi try karo
"Book Appointment" tab pe wapas jao
Wahi Doctor ID 1, koi overlapping time try karo jo already booked ho
Expected: "Something went wrong" ki jagah ab exact backend message dikhna chahiye — "This doctor already has an appointment overlapping this time slot"
Test 3: Find Patient tab
"Find Patient" pe click karo
"Ravi" search karo → Ravi Kumar ka result dikhna chahiye
Screenshots ya results batao — sab pass ho gaye to matlab poora system end-to-end kaam kar raha hai: conflict-free booking, cancellation, doctor schedule, patient search — sab connected. Uske baad hum bachi hui cheezein polish karenge: README documentation (business rules likhna), aur agar time bache to thoda UI/UX refine (jaise doctor/patient dropdown names ke saath, IDs ki jagah).

push karne ka commmand do

Terminal mein root folder se (ya jahan bhi ho, cd /workspaces/Auriga_Project- kar lo pehle):

bash
cd /workspaces/Auriga_Project-
git add .
git commit -m "Add backend (booking, cancellation, exclusion constraint, error handling) and React frontend"
git push
Agar pehli baar push kar rahe ho aur koi upstream/auth issue aaye, to yeh bhi try karo:

bash
git push -u origin main
Agar git status chalao to commit se pehle confirm kar sakte ho kya-kya add ho raha hai:

bash
git status
Push ho jaye to confirm kar dena — phir README mein business rules (2-hour cancellation threshold, ₹100 fee) document kar lete hain taaki submission complete ho jaye.

A busy clinic with a few doctors. The front desk books patients into time slots, but keeps double-booking a doctor or letting two patients grab the same slot. Patients cancel — if they cancel in good time it’s free, but a late cancellation should carry a small fee. The desk needs to see a doctor’s day, find a patient’s appointment by name, and never let two appointments for the same doctor overlap.
Build the front desk something so no doctor is ever double-booked and late cancellations are handled fairly.
(The desk’s frustrations are the spec — build it for any clinic. Get conflict-free booking and the cancellation rule right first, then the lookups.)

Twists for this problem
Level 1 — T6 (lifecycle): “Reschedule an appointment to a new time; it must stay conflict-free (re-check overlap) and keep the same patient and doctor.”
Level 2 — T1 (integrate): “Each morning, remind patients of today’s appointments via the Notification Service.” Graded via /outbox after POST /clock.
Level 3 — T2 (automation): “A job auto-marks appointments as no-show 30 min after their start if not completed.” Graded via POST /clock.

Planning a virtual clock and notification outbox design.
Samajh gaya — yeh teenon "twists" hain jo assignment mein add hue hain, level-wise complexity ke saath. Chalo pehle inko samajhte hain, phir sahi order mein banayenge.

Twists ka matlab (decode karte hain)
Level 1 — Reschedule (T6):

Existing appointment ka time change karna hai, same doctor + same patient rakhte hue.
Naya time bhi conflict-free hona chahiye — matlab humara exclusion constraint dobara kaam aayega, bas ab "update" operation pe bhi apply hoga (abhi sirf insert pe tha).
Level 2 — Morning reminders (T1):

Yeh thoda tricky hai — isme ek "virtual clock" concept lagta hai (grading POST /clock se hoti hai, real system time se nahi).
Matlab hume ek fake/controllable clock banana padega jo LocalDateTime.now() ki jagah use hoga, taaki grader time ko manually aage badha sake (jaise "abhi subah 8 baje hai" simulate karna).
Jab clock "morning" cross kare, system ko us din ke saare appointments ke patients ko ek "notification" bhejni hai — lekin asli SMS/email nahi, balki ek "Notification Service" ka outbox — matlab notifications ek table/list mein store honge, aur GET /outbox se dikhaye jayenge (grader wahi check karega).
Level 3 — Auto no-show (T2):

Same virtual clock use hoga.
Jab clock kisi appointment ke start time se 30 min aage nikal jaye, aur wo appointment abhi bhi "BOOKED" hai (COMPLETED nahi hua), to system usko automatically NO_SHOW status de de.
Yeh bhi POST /clock ke through trigger/grade hota hai.
Core insight: Level 2 aur 3 dono ek hi cheez pe depend karte hain — ek Virtual Clock service, jise hum POST /clock se set karenge, aur jab bhi clock advance ho, hum "kya-kya due hai" check karke actions fire karenge (reminders bhejna, no-shows mark karna). Isliye pehle Clock banana sabse efficient hoga, fir dono features usi pe base honge.

Implementation order (suggest karta hoon)
Reschedule (Level 1) — sabse simple, existing booking logic hi extend hoga. ~15 min.
Virtual Clock foundation — ClockService banayenge jisme ek stored "current time" hoga (default = real time), POST /clock se set/advance hoga. Poore codebase mein jahan bhi LocalDateTime.now() tha (jaise cancellation), usko is service se replace karna padega.
Notification outbox (Level 2) — Notification entity + table, NotificationService, aur clock-set hone pe ek check: "aaj ke BOOKED appointments ke liye reminder banao (agar pehle se nahi bana)". GET /outbox endpoint.
No-show automation (Level 3) — clock-set hone pe ek aur check: "jin appointments ka start_time + 30min < current clock time hai aur status abhi BOOKED hai, unhe NO_SHOW kar do".
Dono Level 2 aur 3 ke checks POST /clock ke andar hi trigger honge (jab bhi clock advance ho, dono jobs chal jayein) — isse grading harness simple rahega.

Confirm karo — is order se thik hai? Agar haan, to Step 1: Reschedule se shuru karta hoon.

Building out a notification service with tests.
Building out a notification service with tests.
Clock set ho gaya (confirm second output se — pehle wale mein output khaali dikh raha tha, shayad terminal display issue, lekin time sahi apply hua). Clock foundation ready hai. ✅

Step 3: Level 2 — Morning reminders (Notification outbox)
A. Migration — notifications table
bash
touch backend/src/main/resources/db/migration/V3__add_notifications.sql
sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT NOT NULL REFERENCES appointments(id),
    type VARCHAR(30) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    -- Prevents sending the same reminder twice for the same appointment
    CONSTRAINT uq_notification_appt_type UNIQUE (appointment_id, type)
);
Samjho UNIQUE (appointment_id, type) kyun: Agar POST /clock baar-baar call ho (jaise grader kai baar clock advance kare), humein har baar dobara reminder nahi bhejna — yeh constraint DB level pe hi duplicate insert ko block kar dega, humein application code mein manually check karne ki zaroorat kam ho jayegi (though hum check bhi karenge, safety ke liye dono).

B. Notification.java entity
model/Notification.java:

java
package com.auriga.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "appointment_id", nullable = false)
    private Long appointmentId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private String message;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
model/NotificationType.java:

java
package com.auriga.clinic.model;

public enum NotificationType {
    APPOINTMENT_REMINDER
}
C. NotificationRepository.java
repository/NotificationRepository.java:

java
package com.auriga.clinic.repository;

import com.auriga.clinic.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();
}
D. NotificationService.java
service/NotificationService.java:

java
package com.auriga.clinic.service;

import com.auriga.clinic.model.*;
import com.auriga.clinic.repository.AppointmentRepository;
import com.auriga.clinic.repository.NotificationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final AppointmentRepository appointmentRepo;
    private final NotificationRepository notificationRepo;

    public NotificationService(AppointmentRepository appointmentRepo,
                                NotificationRepository notificationRepo) {
        this.appointmentRepo = appointmentRepo;
        this.notificationRepo = notificationRepo;
    }

    @Transactional
    public void sendTodaysReminders(LocalDateTime clockNow) {
        LocalDate today = clockNow.toLocalDate();

        List<Appointment> todaysAppointments = appointmentRepo.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
                .filter(a -> a.getStartTime().toLocalDate().equals(today))
                .toList();

        for (Appointment appt : todaysAppointments) {
            Notification notification = Notification.builder()
                    .appointmentId(appt.getId())
                    .type(NotificationType.APPOINTMENT_REMINDER)
                    .message("Reminder: " + appt.getPatient().getName() +
                            " has an appointment with " + appt.getDoctor().getName() +
                            " at " + appt.getStartTime())
                    .createdAt(clockNow)
                    .build();

            try {
                notificationRepo.save(notification);
            } catch (DataIntegrityViolationException e) {
                // Already sent for this appointment — safe to ignore (idempotent)
            }
        }
    }

    public List<Notification> getOutbox() {
        return notificationRepo.findAllByOrderByCreatedAtDesc();
    }
}
Samjho kya ho raha hai: findAll().stream().filter(...) thoda inefficient hai bade datasets ke liye (poori table load kar raha hai), lekin assignment ke scale ke liye theek hai — beginner-friendly aur samajhne mein aasan. Agar aage scale badhana ho, to isko ek proper SQL query (WHERE start_time::date = :today AND status = 'BOOKED') se replace kar sakte hain.

E. ClockController ko update karo — clock set hote hi reminders trigger ho
ClockController.java mein NotificationService inject karo:

java
private final ClockService clockService;
private final NotificationService notificationService;   // NEW

public ClockController(ClockService clockService, NotificationService notificationService) {
    this.clockService = clockService;
    this.notificationService = notificationService;   // NEW
}

@PostMapping
public ResponseEntity<Map<String, LocalDateTime>> setClock(@RequestBody SetClockRequest req) {
    clockService.setTime(req.getTime());
    notificationService.sendTodaysReminders(clockService.now());   // NEW
    return ResponseEntity.ok(Map.of("currentTime", clockService.now()));
}
Import add karo:

java
import com.auriga.clinic.service.NotificationService;
F. Outbox endpoint
Naya controller controller/NotificationController.java:

java
package com.auriga.clinic.controller;

import com.auriga.clinic.model.Notification;
import com.auriga.clinic.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/outbox")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getOutbox() {
        return ResponseEntity.ok(notificationService.getOutbox());
    }
}
G. Restart aur test karo
bash
./mvnw spring-boot:run
Pehle ek appointment banao "aaj" (naye clock ke hisaab se) ke liye — pehle clock set karo, fir dekho kaunsa din hai, us din ka appointment banao:

bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"doctorId": 1, "patientId": 1, "startTime": "2026-09-26T15:00:00", "endTime": "2026-09-26T15:30:00"}'
Ab clock ko usi din ki subah pe set karo (yeh reminder trigger karega):

bash
curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time": "2026-09-26T08:00:00"}'
Outbox check karo:

bash
curl http://localhost:8080/outbox
Expected: ek notification object, jisme "type":"APPOINTMENT_REMINDER" aur message ho.

Duplicate-prevention test — clock dobara set karo (same day):

bash
curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time": "2026-09-26T09:00:00"}'

curl http://localhost:8080/outbox
Expected: outbox mein abhi bhi sirf ek hi notification honi chahiye us appointment ke liye (duplicate nahi bana).

Results paste karo, phir Level 3: Auto no-show pe jayenge.

@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{"doctorId": 1, "patientId": 1, "startTime": "2026-09-26T15:00:00", "endTime": "2026-09-26T15:30:00"}'
{"id":6,"doctor":{"id":1,"name":"Dr. Mehta","specialization":"General"},"patient":{"id":1,"name":"Ravi Kumar","phone":"9999999999"},"startTime":"2026-09-26T15:00:00","endTime":"2026-09-26T15:30:00","status":"BOOKED","cancelledAt":null,"cancellationFee"@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ curl -X POST http://localhost:8080/clock \k \
  -H "Content-Type: application/json" \
  -d '{"time": "2026-09-26T08:00:00"}'
@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ curl http://localhost:8080/outbox) $ curl http://localhost:8080/outbox
[{"id":1,"appointmentId":5,"type":"APPOINTMENT_REMINDER","message":"Reminder: Ravi Kumar has an appointment with Dr. Mehta at 2026-09-26T11:00","createdAt":"2026-09-26T08:00:00"},{"id":2,"appointmentId":6,"type":"APPOINTMENT_REMINDER","message":"Reminder: Ravi Kumar has an appointment with Dr. Mehta at 2026-09-26T15:00","createdAt":"2026-09-26T08:00:00"}]@pranjalprog ➜ /works@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time": "2026-09-26T09:00:00"}'

curl http://localhost:8080/outbox
{"timestamp":"2026-09-17T11:41:11.676Z","status":500,"error":"Internal Server Error","path":"/clock"}[{"id":1,"appointmentId":5,"type":"APPOINTMENT_REMINDER","message":"Reminder: Ravi Kumar has an appointment with Dr. Mehta at 2026-09-26T11:00","createdAt":"2026-09-26T08:00:00"},{"id":2,"appointmentId":6,"type":"APPOINTMENT_REMINDER","message":"Reminder: Ravi Kumar has an appointment with Dr. Mehta at 2026-09-26T15:00","createdAt":"2026-09-26T08:00:00"}]@pranjalprog ➜ /workspaces/Auriga_Project- (main) $

Verifying reminders were correctly applied to rescheduled appointments.
Verifying reminders were correctly applied to rescheduled appointments.
Yeh ek classic Postgres transaction behavior hai — samjho: jab humne notificationRepo.save() ko try/catch mein wrap kiya, humne socha ki duplicate wali insert bas skip ho jayegi aur baaki chalta rahega. Lekin Postgres mein, ek baar koi statement transaction ke andar fail ho jaye, to poora transaction "aborted" state mein chala jaata hai — chahe Java code us specific exception ko catch kar le, baaki saare statements (aur final commit) bhi fail ho jayenge jab tak transaction rollback na ho. Isliye doosri baar clock set karne pe 500 aaya.

Fix: Insert karne se pehle hi check karo ki notification already exist karta hai ya nahi (query se), catch-and-ignore pe depend mat karo.

NotificationRepository.java mein method add karo:
java
package com.auriga.clinic.repository;

import com.auriga.clinic.model.Notification;
import com.auriga.clinic.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByCreatedAtDesc();

    // NEW — check before insert, avoid relying on DB exception mid-transaction
    boolean existsByAppointmentIdAndType(Long appointmentId, NotificationType type);
}
NotificationService.java — sendTodaysReminders method ko replace karo:
java
@Transactional
public void sendTodaysReminders(LocalDateTime clockNow) {
    LocalDate today = clockNow.toLocalDate();

    List<Appointment> todaysAppointments = appointmentRepo.findAll().stream()
            .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
            .filter(a -> a.getStartTime().toLocalDate().equals(today))
            .toList();

    for (Appointment appt : todaysAppointments) {
        // Check BEFORE inserting — avoids Postgres aborting the whole
        // transaction on a duplicate-key error mid-loop
        boolean alreadySent = notificationRepo.existsByAppointmentIdAndType(
                appt.getId(), NotificationType.APPOINTMENT_REMINDER);

        if (alreadySent) {
            continue;
        }

        Notification notification = Notification.builder()
                .appointmentId(appt.getId())
                .type(NotificationType.APPOINTMENT_REMINDER)
                .message("Reminder: " + appt.getPatient().getName() +
                        " has an appointment with " + appt.getDoctor().getName() +
                        " at " + appt.getStartTime())
                .createdAt(clockNow)
                .build();

        notificationRepo.save(notification);
    }
}
(try/catch around save() ab hata diya — ab zaroorat nahi kyunki hum pehle hi check kar rahe hain. DataIntegrityViolationException import bhi remove kar sakte ho agar kahin aur use nahi ho raha.)

Restart aur dobara test karo:
bash
./mvnw spring-boot:run
bash
curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time": "2026-09-26T09:00:00"}'

curl http://localhost:8080/outbox
Is baar 500 error nahi aana chahiye, aur outbox mein abhi bhi sirf wahi 2 notifications honi chahiye (duplicate nahi banna chahiye).

Result paste karo, phir Level 3: Auto no-show pe jayenge.



You are out of free messages until 7:30 PM
Write a message…


Claude is AI and can make mistakes. Please double-check responses.



#Chatgpt

Continued from Appointment Booking Design (1)


Ab Level 2 successfully pass ho gaya hai, so Level 3: Auto No-Show implement karte hain.

Goal:

POST /clock
     ↓
ClockService current time set
     ↓
Notification reminders check
     ↓
No-show check
     ↓
If appointment.startTime + 30 min <= clockNow
AND status == BOOKED
     ↓
status = NO_SHOW
Step 1 — AppointmentStatus check karo
AppointmentStatus.java open karo. Isme NO_SHOW hona chahiye:

public enum AppointmentStatus {
    BOOKED,
    CANCELLED,
    COMPLETED,
    NO_SHOW
}
Agar NO_SHOW already hai, kuch change mat karo.

Step 2 — AppointmentService mein method add karo
AppointmentService.java mein ye method add karo:

@Transactional
public void markNoShows(LocalDateTime clockNow) {

    List<Appointment> appointments = appointmentRepo.findAll().stream()
            .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
            .filter(a -> !clockNow.isBefore(a.getStartTime().plusMinutes(30)))
            .toList();

    for (Appointment appointment : appointments) {
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepo.save(appointment);
    }
}
Logic
Suppose appointment:

Start:       11:00
30 min later:11:30
Clock:

11:29 → BOOKED
11:30 → NO_SHOW
11:45 → NO_SHOW
CANCELLED ya COMPLETED appointment touch nahi hogi because hum sirf:

.filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
check kar rahe hain.

Step 3 — ClockController mein no-show trigger karo
Ab tumhare current ClockController mein AppointmentService inject karna hai.

Current constructor:

public ClockController(ClockService clockService,
                       NotificationService notificationService) {
    this.clockService = clockService;
    this.notificationService = notificationService;
}
isko:

private final AppointmentService appointmentService;

public ClockController(ClockService clockService,
                       NotificationService notificationService,
                       AppointmentService appointmentService) {
    this.clockService = clockService;
    this.notificationService = notificationService;
    this.appointmentService = appointmentService;
}
Aur imports mein:

import com.auriga.clinic.service.AppointmentService;
Step 4 — POST /clock mein no-show call
Tumhara current method:

@PostMapping
public ResponseEntity<Map<String, LocalDateTime>> setClock(
        @RequestBody SetClockRequest req) {

    clockService.setTime(req.getTime());

    notificationService.sendTodaysReminders(
            clockService.now()
    );

    return ResponseEntity.ok(
            Map.of("currentTime", clockService.now())
    );
}
isko:

@PostMapping
public ResponseEntity<Map<String, LocalDateTime>> setClock(
        @RequestBody SetClockRequest req) {

    clockService.setTime(req.getTime());

    LocalDateTime now = clockService.now();

    notificationService.sendTodaysReminders(now);
    appointmentService.markNoShows(now);

    return ResponseEntity.ok(
            Map.of("currentTime", now)
    );
}
Important: now ek baar liya hai, taaki reminder aur no-show dono same virtual clock time use karein.

Step 5 — Compile karo
cd /workspaces/Auriga_Project-/backend
mvn spring-boot:run
Agar startup successful ho, tab testing karenge.

Step 6 — No-show test
Tumhare appointment 5 ka time:

2026-09-26 11:00
Ab clock ko 11:29 set karo:

curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time":"2026-09-26T11:29:00"}'
Appointment 5 abhi:

BOOKED
Phir 11:30:

curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time":"2026-09-26T11:30:00"}'
Ab appointment 5 NO_SHOW honi chahiye.

Check:

curl http://localhost:8080/api/appointments/patient/1
Important Level 2 condition
Is test ke baad:

curl http://localhost:8080/outbox
2 existing notifications hi rehni chahiye.

No-show ke liye notification create nahi kar rahe hain, so outbox mein third notification nahi aani chahiye.

Pehle Step 1–4 implement karo aur mvn spring-boot:run ka result paste karo. Uske baad actual no-show test karenge.


### Step 2 — AppointmentService mein method add karo

AppointmentService.java mein ye method add karo:



---------isko add karke puri file do

@Transactional
public void markNoShows(LocalDateTime clockNow) {

    List<Appointment> appointments = appointmentRepo.findAll().stream()
            .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
            .filter(a -> !clockNow.isBefore(a.getStartTime().plusMinutes(30)))
            .toList();

    for (Appointment appointment : appointments) {
        appointment.setStatus(AppointmentStatus.NO_SHOW);
        appointmentRepo.save(appointment);
    }
}--------
package com.auriga.clinic.service;

import com.auriga.clinic.exception.ResourceNotFoundException;
import com.auriga.clinic.exception.SlotConflictException;
import com.auriga.clinic.model.*;
import com.auriga.clinic.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final ClockService clockService;

    private static final long LATE_CANCEL_THRESHOLD_HOURS = 2;
    private static final BigDecimal LATE_CANCEL_FEE = new BigDecimal("100.00");

    public AppointmentService(AppointmentRepository appointmentRepo,
                              DoctorRepository doctorRepo,
                              PatientRepository patientRepo,
                              ClockService clockService) {
        this.appointmentRepo = appointmentRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.clockService = clockService;
    }

    @Transactional
    public Appointment bookAppointment(Long doctorId, Long patientId,
                                       LocalDateTime start, LocalDateTime end) {

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found: " + doctorId));

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found: " + patientId));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .startTime(start)
                .endTime(end)
                .status(AppointmentStatus.BOOKED)
                .cancellationFee(BigDecimal.ZERO)
                .build();

        try {
            return appointmentRepo.save(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new SlotConflictException(
                    "This doctor already has an appointment overlapping this time slot");
        }
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment is already cancelled");
        }

        LocalDateTime cancelledAt = clockService.now();

        Duration noticeGiven = Duration.between(
                cancelledAt,
                appointment.getStartTime()
        );

        boolean isLateCancel = noticeGiven.toHours() < LATE_CANCEL_THRESHOLD_HOURS;

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(cancelledAt);
        appointment.setCancellationFee(
                isLateCancel ? LATE_CANCEL_FEE : BigDecimal.ZERO
        );

        return appointmentRepo.save(appointment);
    }

    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId,
                                             LocalDateTime newStart,
                                             LocalDateTime newEnd) {

        if (!newEnd.isAfter(newStart)) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot reschedule a cancelled appointment"
            );
        }

        appointment.setStartTime(newStart);
        appointment.setEndTime(newEnd);

        try {
            return appointmentRepo.save(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new SlotConflictException(
                    "New time conflicts with another appointment for this doctor"
            );
        }
    }

    public List<Appointment> getDoctorSchedule(Long doctorId) {
        return appointmentRepo.findByDoctorIdAndStatusOrderByStartTimeAsc(
                doctorId,
                AppointmentStatus.BOOKED
        );
    }

    public List<Patient> searchPatientsByName(String name) {
        return patientRepo.findByNameContainingIgnoreCase(name);
    }

    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
}

Haan. Tumhari existing AppointmentService.java mein sirf markNoShows() method add karke complete file ye rahegi.

Is poori file ko replace karo. ```java wali lines paste mat karna.

package com.auriga.clinic.service;

import com.auriga.clinic.exception.ResourceNotFoundException;
import com.auriga.clinic.exception.SlotConflictException;
import com.auriga.clinic.model.*;
import com.auriga.clinic.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;
    private final DoctorRepository doctorRepo;
    private final PatientRepository patientRepo;
    private final ClockService clockService;

    private static final long LATE_CANCEL_THRESHOLD_HOURS = 2;
    private static final BigDecimal LATE_CANCEL_FEE = new BigDecimal("100.00");

    public AppointmentService(AppointmentRepository appointmentRepo,
                              DoctorRepository doctorRepo,
                              PatientRepository patientRepo,
                              ClockService clockService) {
        this.appointmentRepo = appointmentRepo;
        this.doctorRepo = doctorRepo;
        this.patientRepo = patientRepo;
        this.clockService = clockService;
    }

    @Transactional
    public Appointment bookAppointment(Long doctorId, Long patientId,
                                       LocalDateTime start, LocalDateTime end) {

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Doctor doctor = doctorRepo.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found: " + doctorId));

        Patient patient = patientRepo.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found: " + patientId));

        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .startTime(start)
                .endTime(end)
                .status(AppointmentStatus.BOOKED)
                .cancellationFee(BigDecimal.ZERO)
                .build();

        try {
            return appointmentRepo.save(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new SlotConflictException(
                    "This doctor already has an appointment overlapping this time slot");
        }
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment is already cancelled");
        }

        LocalDateTime cancelledAt = clockService.now();

        Duration noticeGiven = Duration.between(
                cancelledAt,
                appointment.getStartTime()
        );

        boolean isLateCancel = noticeGiven.toHours() < LATE_CANCEL_THRESHOLD_HOURS;

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(cancelledAt);
        appointment.setCancellationFee(
                isLateCancel ? LATE_CANCEL_FEE : BigDecimal.ZERO
        );

        return appointmentRepo.save(appointment);
    }

    @Transactional
    public Appointment rescheduleAppointment(Long appointmentId,
                                             LocalDateTime newStart,
                                             LocalDateTime newEnd) {

        if (!newEnd.isAfter(newStart)) {
            throw new IllegalArgumentException(
                    "End time must be after start time"
            );
        }

        Appointment appointment = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found: " + appointmentId));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot reschedule a cancelled appointment"
            );
        }

        appointment.setStartTime(newStart);
        appointment.setEndTime(newEnd);

        try {
            return appointmentRepo.save(appointment);
        } catch (DataIntegrityViolationException e) {
            throw new SlotConflictException(
                    "New time conflicts with another appointment for this doctor"
            );
        }
    }

    @Transactional
    public void markNoShows(LocalDateTime clockNow) {

        List<Appointment> appointments = appointmentRepo.findAll().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
                .filter(a -> !clockNow.isBefore(
                        a.getStartTime().plusMinutes(30)
                ))
                .toList();

        for (Appointment appointment : appointments) {
            appointment.setStatus(AppointmentStatus.NO_SHOW);
            appointmentRepo.save(appointment);
        }
    }

    public List<Appointment> getDoctorSchedule(Long doctorId) {
        return appointmentRepo.findByDoctorIdAndStatusOrderByStartTimeAsc(
                doctorId,
                AppointmentStatus.BOOKED
        );
    }

    public List<Patient> searchPatientsByName(String name) {
        return patientRepo.findByNameContainingIgnoreCase(name);
    }

    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return appointmentRepo.findByPatientId(patientId);
    }
}
Abhi kya check karna hai
Sabse pehle ensure karo ki AppointmentStatus.java mein:

NO_SHOW
present hai.

Phir:

cd /workspaces/Auriga_Project-/backend
mvn spring-boot:run
Agar BUILD SUCCESS / application started ho jaye, uska result paste karo. Uske baad ClockController mein Level 3 trigger add karenge.


@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time":"2026-09-26T11:29:00"}'
{"currentTime":"2026-09-26T11:29:00"}@pranjalprog ➜ /workspaces@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ curl -X POST http://localhost:8080/clock \
  -H "Content-Type: application/json" \
  -d '{"time":"2026-09-26T11:30:00"}'
@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ curl http://localhost:8080/outbox) $ curl http://localhost:8080/outbox
[{"id":1,"appointmentId":5,"type":"APPOINTMENT_REMINDER","message":"Reminder: Ravi Kumar has an appointment with Dr. Mehta at 2026-09-26T11:00","createdAt":"2026-09-26T08:00:00"},{"id":2,"appointmentId":6,"type":"APPOINTMENT_REMINDER","message":"Reminder: Ravi Kumar has an appointment with Dr. Mehta at 2026-09-26T15:00","createdAt":"2026-09-26T08:00:00"}]@pranjalprog ➜ /workspaces/Auriga_Project- (main) $ 

Yahan important point hai: tumhara markNoShows() method add ho gaya hai, but POST /clock se woh trigger nahi ho raha.

Evidence:

11:29 → appointment 5 should remain BOOKED

11:30 → appointment 5 should become NO_SHOW

/outbox mein 2 notifications rehna correct hai; no-show outbox mein notification nahi banata.

Lekin abhi humein appointment status verify karna hai.

1. Pehle appointment status check karo
Run:

curl http://localhost:8080/api/appointments/patient/1
Agar appointment 5 abhi bhi:

"status":"BOOKED"
hai, then next step definitely ClockController update hai.

2. ClockController mein ye call hona chahiye
POST /clock ke andar:

LocalDateTime now = clockService.now();

notificationService.sendTodaysReminders(now);
appointmentService.markNoShows(now);
Aur AppointmentService inject hona chahiye:

private final AppointmentService appointmentService;
Constructor:

public ClockController(ClockService clockService,
                       NotificationService notificationService,
                       AppointmentService appointmentService) {
    this.clockService = clockService;
    this.notificationService = notificationService;
    this.appointmentService = appointmentService;
}
3. Important
Outbox mein abhi bhi exactly 2 notifications hona correct result hai. markNoShows() sirf appointment ka status change karta hai:

Appointment 5
11:00 start
   ↓
11:30 virtual clock
   ↓
BOOKED → NO_SHOW
Outbox:

Notification 1 → Appointment 5
Notification 2 → Appointment 6
Third notification nahi aani chahiye.

Pehle curl http://localhost:8080/api/appointments/patient/1 ka result paste karo. Usse immediately pata chalega ki Level 3 trigger hua ya nahi.







