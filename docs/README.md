# Documentação de testes

## Postman

Arquivos para importação:

- `postman/clientes-api.postman_collection.json`: coleção completa da API.
- `postman/clientes-api-local.postman_environment.json`: endereços e credenciais do ambiente Docker local.

### Como usar

1. Inicie o ambiente com `docker compose up -d --build`.
2. No Postman, use **Import** e selecione os dois arquivos da pasta `docs/postman`.
3. Selecione o environment **Clientes API - Local**.
4. Execute primeiro as requisições da pasta **01 - Autenticação**.
5. Execute as demais requisições individualmente ou use o Collection Runner para rodar a coleção na ordem.

A requisição de cadastro gera automaticamente um CPF válido e guarda o `customerId` retornado. Os tokens ADMIN e USER também são guardados automaticamente após a autenticação.

### Portas personalizadas

Se alguma porta foi alterada no Docker Compose, edite as variáveis `baseUrl`, `keycloakUrl` ou `rabbitmqUrl` no environment do Postman.

### Ordem recomendada

1. Obter token ADMIN.
2. Obter token USER.
3. Verificar health check.
4. Cadastrar e consultar o cliente.
5. Executar consultas, atualização e score.
6. Publicar a alteração de status no RabbitMQ.
7. Consultar novamente o cliente para verificar o novo status.
8. Executar os cenários de erro.
9. Excluir o cliente por último.
