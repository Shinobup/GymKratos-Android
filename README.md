# 🏋️‍♂️ Gym Kratos - Sistema de Gestión Móvil

Aplicación nativa para Android desarrollada para la gestión administrativa, financiera y de clientes de un gimnasio real. El sistema permite el control de membresías, automatización de cobros, análisis de ingresos en tiempo real y gestión multi-perfil.

## 🚀 Características Principales
* **Arquitectura Serverless (SaaS):** Base de datos gestionada en la nube mediante Google Sheets y Google Apps Script (API REST customizada).
* **Sistema Multi-Rol y Privacidad:** Lógica de perfiles (Administradores vs. Profesores). Ocultamiento dinámico de datos financieros y partición de listas de clientes dependiendo de los permisos del usuario activo.
* **Trazabilidad Financiera:** Cálculo en tiempo real de ingresos, desglose automático de ganancias por disciplina y un registro de "Historial" inmutable en el backend para auditorías y cuadre de caja.
* **Módulo de Cobranza Inteligente:** Integración con la API de WhatsApp para el envío automatizado de recordatorios de pago a clientes con membresías vencidas.
* **Gestión Asíncrona Avanzada:** Renderizado dinámico de datos (ordenamiento alfabético en tiempo de ejecución), peticiones HTTP optimizadas y políticas de reintento personalizadas (RetryPolicy) para manejar conexiones inestables.
* **Validación de Datos (Data Integrity):** Filtros estrictos de formato para mantener la coherencia de la base de datos desde el cliente (ej. formatos +569).

## 🛠️ Tecnologías Utilizadas
* **Frontend:** Java (Android SDK), XML, Android Studio.
* **Backend & API:** Google Sheets + Google Apps Script (JavaScript).
* **Peticiones HTTP:** Librería Volley.
* **Formatos de datos:** JSON para comunicación Cliente-Servidor.

## 🛡️ Ciberseguridad y Privacidad
> *Nota: Por motivos de seguridad, para proteger los datos financieros del cliente y prevenir ataques de ingeniería inversa contra el endpoint de la API, **el archivo instalable `.apk` y las URLs de Google Apps Script han sido omitidos/censurados** de este repositorio público.*
> *Para evaluar el código: Clona este repositorio, configura tu propio entorno y reemplaza la variable de entorno correspondiente.*

## 📸 Pantallas de la Aplicación
<img width="250" alt="Screenshot_20260929_174530" src="https://github.com/user-attachments/assets/bdc401db-b480-4ab0-8322-1341940b21d3" />
<img width="250" alt="Screenshot_20260929_174721" src="https://github.com/user-attachments/assets/c2a38ebb-ba2e-45d8-aa2c-b5a6534f301a" />
<img width="250" alt="Screenshot_20260929_174753" src="https://github.com/user-attachments/assets/00e6e9d6-c359-4cbb-b8ed-977ffb7c98db" />
<img width="250" alt="Screenshot_20260929_174913" src="https://github.com/user-attachments/assets/6f2ad7d7-2206-4734-ac3f-126eaef5b5d4" />
<img width="250" alt="Screenshot_20260929_174940" src="https://github.com/user-attachments/assets/08fff3aa-523f-4957-95c7-d28689af5f43" />
<img width="250" alt="Screenshot_20260929_175017" src="https://github.com/user-attachments/assets/5e49f790-4072-4c05-a3f4-12ef71a3782b" />
<img width="250" alt="Screenshot_20260929_175043" src="https://github.com/user-attachments/assets/fd4ff340-f30c-4d81-ae14-19528166f9eb" />
<img width="250" alt="Screenshot_20260929_175106" src="https://github.com/user-attachments/assets/ebf3864a-fa40-49d0-872b-d184e7f4994b" />


---
*Proyecto de software desarrollado por Aliro Cuevas Silva.*
