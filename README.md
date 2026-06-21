# app-mobile

Rede social voltada para moda e compartilhamento de looks

## Tecnologias
Kotlin
.NET
GCP
Postgres

Imagens (bucket)

## Rotas
Cadastro
Login
Logout
Deletar conta
Read meu usuario
Read posts todos
Read posts por usuario
Criar post
Deletar post
Editar post
Pesquisar por topico


## Desenvolvimento
[x] Definir funcionalidades
[x] Criar bucket para imagens
[x] Wireframe
[x] Definir system design
[x] Definir rotas do back
[x] Codar back
[x] Card de imagem
[x] Tela de perfil
[x] Feed com imagens
[x] Barra de navegação
[x] Pesquisa
[ ] Sistema de notificação
[x] Integração com API de previsão do tempo
[x] Integração com câmera do celular
[ ] testes unitarios no back
[ ] acessibilidade no app

como executar
dotnet build src/backend/core/Hanger.Application/
dotnet run --project src/backend/Presentation/Hanger.WebApi/Hanger.csproj --urls http://localhost:5089
