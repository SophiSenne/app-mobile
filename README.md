# Hanger — Rede Social de Looks

> *"Vista sua história"*

Hanger é uma rede social mobile para compartilhar e descobrir looks, conectando a escolha do que vestir ao **clima atual** e à **ocasião do momento**. A dor sanada: quando você quer se vestir bem e não sabe por onde começar, o Hanger entrega inspirações reais de outras pessoas, filtradas pelo clima e estilo.

## Funcionalidades principais

| # | Funcionalidade | Tecnologia |
|---|---|---|
| 1 | Autenticação login / cadastro | .NET API |
| 2 | Feed de looks com filtros por categoria e clima | Kotlin |
| 3 | Tela de exploração (busca por posts e usuários) | Retrofit + REST |
| 4 | Detalhe do post com curtidas, comentários e compartilhamento | Android Share Intent |
| 5 | Criação de post via câmera ou galeria | Android Camera + FileProvider |
| 6 | Perfil com posts e seguidores |  |
| 7 | Notificações (curtidas, comentários, novos seguidores) | Foreground Service + polling |
| 8 | Previsão do tempo em tempo real | Open-Meteo API (sem key) |

## Telas

`Login` → `Cadastro (step 1 / step 2)` → `Feed` → `Explorar` → `Criar Post` → `Detalhe do Post` → `Perfil` → `Notificações`

## Stack

**Mobile:** Kotlin · Jetpack Compose · MVVM · Retrofit · Coil

**Backend:** .NET (C#) · PostgreSQL · GCP · Supabase Storage

**API Externa:** [Open-Meteo](https://open-meteo.com/) — clima por latitude/longitude, sem chave de API

## Como executar

### Backend
```bash
dotnet build src/backend/core/Hanger.Application/
dotnet run --project src/backend/Presentation/Hanger.WebApi/Hanger.csproj --urls http://localhost:5089
```

### App Android
Abra `src/app/` no Android Studio e rode em um dispositivo físico ou emulador (minSdk 24 / API 24+).

Configure o endpoint da API no `RetrofitClient.kt` apontando para o host correto.

## Estrutura do repositório

```
app-mobile/
├── src/
│   ├── app/          # Projeto Android (Kotlin + Compose)
│   └── backend/      # Script SQL do banco de dados
├── docs/             # Documentação completa, identidade visual e wireframes
└── README.md
```

## Documentação

Veja [docs/docs.md](docs/docs.md) para a documentação técnica completa: arquitetura, banco de dados, fluxo de telas, uso de hardware e instruções detalhadas.

## Vídeo Demonstrativo

Veja o vídeo demonstrativo do app [aqui]().
