# KKU Incident Map (เว็บปักหมุดแจ้งเหตุ มข.)

เว็บแอปสำหรับปักหมุดแจ้งเหตุฉุกเฉินในพื้นที่มหาวิทยาลัยขอนแก่น แบบเรียลไทม์ ใช้ Spring Boot + Thymeleaf + Leaflet และฐานข้อมูล H2 (ในหน่วยความจำ)

## คุณสมบัติหลัก
- ปักหมุดตำแหน่งบนแผนที่และส่งรายงานเหตุ
- แสดงรายการเหตุและหมุดบนแผนที่แบบเรียลไทม์
- ปรับสถานะเหตุโดยเจ้าหน้าที่ (ต้องล็อกอิน Basic Auth)
- ธีมสีม่วงเข้ากับโทนมหาวิทยาลัยขอนแก่น

### เอกสาร API (Swagger/OpenAPI)
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### AI Prototype (จัดหมวดเหตุและความเร่งด่วน)
- Endpoint: `POST /api/ai/classify`
  - Body ตัวอย่าง: `{ "text": "รถชนหน้าประตู มีคนบาดเจ็บ" }`
  - ผลลัพธ์ตัวอย่าง: `{ "type": "accident", "severity": "high", "confidence": 0.8 }`
- หมายเหตุ: เป็น rule-based placeholder เพื่อสาธิตการเชื่อม AI สามารถเปลี่ยนไปเรียกบริการ AI จริง (เช่น Azure OpenAI) ผ่าน REST ได้โดยไม่ต้องเปลี่ยนหน้าเว็บ

## การรันบน Windows (PowerShell)
```powershell
# สร้างและรัน
mvn -v
mvn clean spring-boot:run
```
เปิดเบราว์เซอร์ไปที่ `http://localhost:8080`

- บัญชี Basic Auth (สำหรับเรียก API update สถานะ):
  - ผู้ใช้: `officer` รหัส: `kku1234`
  - ผู้ใช้: `rescue` รหัส: `kku1234`

## หมายเหตุ
- โปรเจกต์นี้เป็นต้นแบบสำหรับทดสอบบนเครื่อง (Local) เท่านั้น
- ฐานข้อมูล H2 เป็นแบบ In-Memory (ข้อมูลจะหายเมื่อหยุดแอป)
