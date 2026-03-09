# Reglas — Frontend (Refine + Ant Design)

## Dinero
- El frontend NUNCA calcula montos — siempre consume valores pre-calculados desde la API
- Los montos llegan en centavos (BIGINT) → dividir entre 100 solo para mostrar al usuario
- Nunca hacer operaciones aritméticas con montos en el frontend

## Comunicación con el backend
- El frontend SOLO llama al Spring Boot API, nunca a Supabase directamente
- Toda lectura y escritura de datos pasa por los endpoints REST del backend
- La URL base del API se configura via `VITE_API_URL` (variable de entorno)

## Adjuntos
- El upload de comprobantes (PDFs) se hace a través del backend, no directamente a Supabase Storage
- El frontend solo muestra la URL recibida desde la API

## UI
- Los campos de metadata varían según `payment_type` — renderizar dinámicamente según el tipo seleccionado
- Nunca hardcodear campos de metadata para un payment_type específico
