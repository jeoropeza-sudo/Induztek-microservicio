# Reporte de pruebas de microservicios

**Fecha:** 2026-11-23  
**Proyecto:** Induztek-microservicio

## Resumen

| Módulo | Pruebas | Fallos | Errores |
|---|---:|---:|---:|
| auth | 4 | 0 | 0 |
| usuarios | 3 | 0 | 0 |
| empresas | 4 | 0 | 0 |
| equipos | 3 | 0 | 0 |
| instrumentos | 3 | 0 | 0 |
| protocolos | 3 | 0 | 0 |
| apiGateway | 1 | 0 | 0 |
| eureka | 1 | 0 | 0 |
| **Total** | **22** | **0** | **0** |

Todos los comandos `mvnw test` terminaron con código 0. Se agregaron pruebas unitarias para autenticación, validación de usuarios y empresas, creación de equipos, instrumentos y protocolos. Los módulos con base de datos también prueban guardar y volver a consultar registros usando H2.

## Entorno y limitaciones

- JDK usado: 25 incluido en Android Studio.
- Docker: no disponible; se usó H2 en memoria para las pruebas.
- Node.js: disponible. Playwright no aplica porque estos módulos exponen APIs y no tienen interfaz web.
- No se probaron endpoints contra servicios iniciados conjuntamente ni se verificó un servidor SMTP real.
- H2 no reemplaza una validación con MySQL; la compatibilidad específica de base de datos aún debe probarse.

**Resultado:** pruebas automatizadas aprobadas; validación E2E de servicios reales y SMTP pendiente.
