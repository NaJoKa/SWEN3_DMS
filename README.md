# Document Management Systema Document 
management system for archiving documents in a FileStore,
with automatic OCR (queue for OC-recognition),
automatic summary generation (using Gen-AI),
tagging and full text search (ElasticSearch).


nur ein Vorschlag: 

paperless-project/
│── docker-compose.yml         # startet alle Services
│── README.md                  # Projektübersicht
│── docs/                      # Architekturdiagramme, OpenAPI Spezifikationen
│── scripts/                   # Hilfsskripte für Setup/Deployment
│
├── web-ui/                    # PaperlessWebUI (nginx + frontend)
│   ├── Dockerfile
│   └── src/
│
├── rest-api/                  # PaperlessREST (Backend API)
│   ├── Dockerfile
│   ├── openapi.yml
│   └── src/
│
├── services/                  # PaperlessServices (Worker)
│   ├── Dockerfile
│   ├── ocr-worker/
│   │   └── src/
│   └── genai-worker/
│       └── src/
│
├── infra/                     # Infrastruktur-Config
│   ├── elasticsearch/
│   ├── minio/
│   ├── postgres/
│   ├── rabbitmq/
│   └── adminer/
│
└── .github/
    └── workflows/             # CI/CD Pipelines (z.B. GitHub Actions)

