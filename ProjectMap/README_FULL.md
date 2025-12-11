# ระบบเว็บปักหมุดแจ้งเหตุในมหาวิทยาลัยขอนแก่น
## KKU Incident Map System

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-17+-green)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-green)
![License](https://img.shields.io/badge/License-MIT-green)

## 📋 คำอธิบายโครงการ

ระบบปักหมุดแจ้งเหตุในมหาวิทยาลัยขอนแก่น (KKU Incident Map System) เป็นเว็บแอปพลิเคชันที่ออกแบบมาเพื่อให้นักศึกษา บุคลากร และประชาชนสามารถแจ้งเหตุการณ์ฉุกเฉินในมหาวิทยาลัยได้แบบเรียลไทม์ พร้อมให้เจ้าหน้าที่รักษาความปลอดภัยสามารถตรวจสอบ ติดตาม และจัดการเหตุการณ์ได้อย่างมีประสิทธิภาพ

### ✨ ฟีเจอร์หลัก

- **🗺️ แผนที่แบบโต้ตอบ** - ใช้ Leaflet.js แสดงตำแหน่งเหตุการณ์แบบสด
- **🚨 รายงานเหตุเร็ว** - ผู้พบเห็นสามารถปักหมุดแจ้งเหตุในเวลาไม่ถึง 1 นาที
- **📊 แดชบอร์ดเจ้าหน้าที่** - มีสถิติ การกรอง และการจัดการเหตุการณ์แบบเรียลไทม์
- **🔄 อัปเดตสถานะ** - เจ้าหน้าที่สามารถอัปเดตสถานะและเพิ่มบันทึก
- **📱 Responsive Design** - ใช้ได้ดีบนทั้ง Desktop และ Mobile
- **🔐 ระบบความปลอดภัย** - ข้อมูลผู้แจ้งเหตุถูกปกป้อง

### 🗂️ ประเภทเหตุการณ์ที่รองรับ

1. **🚗 อุบัติเหตุ** - อุบัติเหตุรถชน บาดเจ็บ
2. **🏥 ผู้ป่วยฉุกเฉิน** - คนเจ็บป่วยต้องการความช่วยเหลือ
3. **⚔️ ทะเลาะวิวาท** - เหตุความขัดแย้ง การทะเลาะ
4. **🔥 เหตุไฟไหม้** - เหตุไฟไหม้เบื้องต้น
5. **🙋 ขอความช่วยเหลือ** - การขอความช่วยเหลือทั่วไป

---

## 🛠️ เทคโนโลยีที่ใช้

### Backend
- **Java 17** - ภาษาโปรแกรม
- **Spring Boot 3.3.4** - Framework หลัก
- **Spring Data JPA** - ORM
- **Spring Security** - ความปลอดภัย
- **H2 Database** - ฐานข้อมูล (สำหรับการพัฒนา)
- **Maven** - Build Tool

### Frontend
- **HTML5** - ด้านข้อความ
- **Bootstrap 5** - CSS Framework
- **Thymeleaf** - Template Engine
- **Leaflet.js** - แผนที่เชิงโต้ตอบ
- **JavaScript (Vanilla)** - ตรรกะด้านข้างไคลเอนต์
- **Font Awesome 6** - ไอคอน

---

## 📦 โครงสร้างโปรเจกต์

```
ProjectMap/
├── src/main/
│   ├── java/org/kku/
│   │   ├── KkuIncidentMapApplication.java      # Main Application
│   │   ├── model/
│   │   │   └── Incident.java                   # Entity Model
│   │   ├── repo/
│   │   │   └── IncidentRepository.java         # Data Access Layer
│   │   ├── service/
│   │   │   └── IncidentService.java            # Business Logic Layer
│   │   ├── web/
│   │   │   └── IncidentController.java         # REST API & Web Controller
│   │   ├── config/
│   │   │   └── SecurityConfig.java             # Security Configuration
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java     # Exception Handler
│   │       └── ResourceNotFoundException.java  # Custom Exception
│   │
│   └── resources/
│       ├── application.properties               # Configuration File
│       └── templates/
│           ├── index.html                       # หน้าแรก (ผู้รายงาน)
│           └── dashboard.html                   # แดชบอร์ด (เจ้าหน้าที่)
│
├── pom.xml                                      # Maven Configuration
└── README.md                                    # Documentation
```

---

## 🚀 วิธีการใช้งาน

### ข้อกำหนดเบื้องต้น
- Java 17 หรือสูงกว่า
- Maven 3.6+
- Git (สำหรับ Clone)

### การติดตั้ง

1. **Clone Repository**
```bash
git clone https://github.com/yourusername/kku-incident-map.git
cd ProjectMap
```

2. **Build Project**
```bash
mvn clean install
```

3. **รันแอปพลิเคชัน**
```bash
mvn spring-boot:run
```

4. **เข้าใช้งาน**
- หน้าแรก (ผู้รายงาน): `http://localhost:8080/`
- แดชบอร์ด (เจ้าหน้าที่): `http://localhost:8080/dashboard`
- H2 Console: `http://localhost:8080/h2-console`

---

## 📖 API Documentation

### Endpoints หลัก

#### ดึงข้อมูลเหตุการณ์
```
GET /api/incidents                    # ดึงเหตุการณ์ทั้งหมด
GET /api/incidents/active             # ดึงเหตุการณ์ที่ยังดำเนินการอยู่
GET /api/incidents/{id}               # ดึงเหตุการณ์ตาม ID
GET /api/incidents/type/{type}        # ดึงเหตุการณ์ตามประเภท
GET /api/incidents/stats              # ดึงสถิติเหตุการณ์
```

#### สร้าง/อัปเดตเหตุการณ์
```
POST /api/incidents                   # สร้างเหตุการณ์ใหม่
PUT /api/incidents/{id}/status        # อัปเดตสถานะเหตุการณ์
DELETE /api/incidents/{id}            # ลบเหตุการณ์
```

### ตัวอย่าง JSON Request

**สร้างเหตุการณ์ใหม่:**
```json
{
  "type": "accident",
  "description": "อุบัติเหตุรถชนบริเวณหน้าคณะวิศวกรรมศาสตร์",
  "latitude": 16.475,
  "longitude": 102.825,
  "reporter": "นักศึกษาคณะ IT",
  "reporterContact": "081-xxx-xxxx",
  "severityLevel": "ด่วน"
}
```

**อัปเดตสถานะ:**
```json
{
  "status": "กำลังดำเนินการ",
  "notes": "หน่วยกู้ภัยกำลังเดินทางไปยังจุดเกิดเหตุ"
}
```

---

## 🎨 การออกแบบ UI/UX

### สีลายแบบ (Color Scheme)
- **Primary**: #8B4513 (สีน้ำตาลของมข.)
- **Secondary**: #D2691E (สีส้มทอง)
- **Light**: #F5E6D3 (สีครีมอ่อน)
- **Dark**: #654321 (สีน้ำตาลเข้ม)

### สถานะเหตุการณ์
- 🔴 **รอการดำเนินการ** - ต้องการการช่วยเหลือเร่งด่วน
- 🟡 **กำลังดำเนินการ** - อยู่ระหว่างการจัดการ
- 🟢 **ดำเนินการแล้ว** - ปิดเคสแล้ว

---

## 🔒 ความปลอดภัย

### การรักษาความปลอดภัยผู้รายงาน
- ผู้รายงานสามารถใช้นามแฝงได้
- ช่องทางติดต่อ (เบอร์โทร/Line) เป็นทางเลือก
- ข้อมูลส่วนบุคคลไม่บังคับต้องระบุชื่อจริง

### Authentication & Authorization
- ปัจจุบัน: Basic Spring Security
- ในอนาคต: JWT Token / OAuth2

---

## 📊 ตัวอย่าง Use Cases

### Use Case 1: ผู้พบเห็นแจ้งอุบัติเหตุ
1. เปิดแอป → คลิกบนแผนที่เพื่อเลือกตำแหน่ง
2. เลือกประเภท "อุบัติเหตุ"
3. เขียนรายละเอียด → ส่งรายงาน
4. ระบบส่งแจ้งเตือนให้เจ้าหน้าที่

### Use Case 2: เจ้าหน้าที่ดูแลเหตุการณ์
1. เข้าแดชบอร์ด → เลือกเหตุการณ์ที่ต้องการ
2. ดูรายละเอียด → คลิก "ดูบนแผนที่"
3. อัปเดตสถานะ → เพิ่มบันทึก
4. ส่งข้อมูลและปิดเคส

---

## 🤝 การมีส่วนร่วม

ยินดีต้อนรับการมีส่วนร่วมจากชุมชน! กรุณา:

1. Fork the repository
2. สร้าง branch ใหม่ (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

## 👥 ติดต่อและสนับสนุน

- **Email**: support@kku.ac.th
- **Website**: www.kku.ac.th
- **Issue Tracker**: GitHub Issues

---

## 🙏 Acknowledgments

- มหาวิทยาลัยขอนแก่น (Khon Kaen University)
- Spring Boot Community
- Leaflet.js Developers
- Bootstrap Team

---

## 📅 Changelog

### Version 1.0.0 (2024-12-09)
- ✅ เปิดตัวเวอร์ชันแรก
- ✅ ระบบรายงานเหตุการณ์พื้นฐาน
- ✅ แดชบอร์ดเจ้าหน้าที่
- ✅ API RESTful สมบูรณ์

---

## 🎯 Road Map

- [ ] ระบบ Notification ผ่าน Email/SMS
- [ ] ระบบ Attachment (รูปภาพ/วิดีโอ)
- [ ] Analytics & Reporting
- [ ] Mobile App (iOS/Android)
- [ ] Multi-language Support
- [ ] Geofencing Alerts
- [ ] Integration กับ Social Media

---

**สร้างด้วย ❤️ สำหรับความปลอดภัยในมหาวิทยาลัยขอนแก่น**
