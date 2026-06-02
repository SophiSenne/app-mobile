# Modelagem do Banco de Dados — Hanger 👗

Sua estrutura inicial está boa! Vou refinar e expandir para cobrir todos os requisitos da atividade (notificações, compartilhamento, clima, etc).

---

## Tabelas refinadas

### `users`
| coluna | tipo | notas |
|---|---|---|
| id | uuid PK | |
| username | varchar unique | |
| email | varchar unique | |
| password_hash | varchar | |
| bio | text | nullable |
| avatar_url | varchar | nullable, bucket |
| location_city | varchar | nullable, para clima padrão |
| created_at | timestamp | |

---

### `posts`
| coluna | tipo | notas |
|---|---|---|
| id | uuid PK | |
| user_id | uuid FK → users | |
| image_url | varchar | bucket |
| title | varchar | |
| caption | text | nullable |
| weather_condition | varchar | ex: "sunny", "rainy" — da API |
| temperature | float | temperatura no momento do post |
| city | varchar | cidade do post |
| share_count | int | default 0 |
| created_at | timestamp | |

---

### `categories`
| coluna | tipo | notas |
|---|---|---|
| id | serial PK | |
| name | varchar unique | ex: "Casual", "Formal", "Esportivo" |

---

### `types`
| coluna | tipo | notas |
|---|---|---|
| id | serial PK | |
| category_id | int FK → categories | |
| name | varchar | ex: categoria "Casual" → tipo "Street", "Básico" |

---

### `post_tags` *(substitui sua Search — é uma relação N:N)*
| coluna | tipo | notas |
|---|---|---|
| post_id | uuid FK → posts | |
| category_id | int FK → categories | nullable |
| type_id | int FK → types | nullable |
| PK composta | (post_id, type_id) | |

> Isso permite buscar posts por categoria/tipo de forma limpa.

---

### `follows`
| coluna | tipo | notas |
|---|---|---|
| follower_id | uuid FK → users | |
| following_id | uuid FK → users | |
| created_at | timestamp | |
| PK composta | (follower_id, following_id) | |

---

### `likes`
| coluna | tipo | notas |
|---|---|---|
| user_id | uuid FK → users | |
| post_id | uuid FK → posts | |
| created_at | timestamp | |
| PK composta | (user_id, post_id) | |

---

### `notifications`
| coluna | tipo | notas |
|---|---|---|
| id | uuid PK | |
| recipient_id | uuid FK → users | quem recebe |
| sender_id | uuid FK → users | quem gerou |
| type | enum | `like`, `follow`, `comment`, `share` |
| post_id | uuid FK → posts | nullable |
| read | boolean | default false |
| created_at | timestamp | |

---

### `device_tokens` *(para push notification)*
| coluna | tipo | notas |
|---|---|---|
| id | uuid PK | |
| user_id | uuid FK → users | |
| token | varchar | FCM/APNs token |
| platform | enum | `android`, `ios` |
| updated_at | timestamp | |

---

## Diagrama de relações

```
users ──< posts ──< post_tags >── types >── categories
  │          │
  │< follows │< likes
  │
  └< notifications
  └< device_tokens
```

---

## O que cada tabela cobre nos requisitos

| Requisito | Tabela |
|---|---|
| Autenticação | `users` |
| Feed / Posts | `posts` |
| Categorização + busca | `categories`, `types`, `post_tags` |
| API de clima | campos em `posts` (weather, temp, city) |
| Sistema de notificações | `notifications` + `device_tokens` |
| Compartilhamento | `share_count` em posts |
| Social (seguir/curtir) | `follows`, `likes` |

---

## Sugestão de stack para o backend

Dado que você tem bastante coisa para codar, sugiro **Supabase** — ele já te dá Postgres, auth, storage (bucket de imagens), e edge functions, economizando muito tempo para focar no Flutter.

Quer que eu ajude a montar as rotas do backend agora?