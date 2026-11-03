# language: pt
@api @monitoramento
Funcionalidade: Informações e saúde da aplicação
  Como responsável pela operação da plataforma ESG
  Quero consultar a versão, o ambiente e a saúde da API
  Para manter a rastreabilidade do que está em execução

  @positivo @governanca
  Cenário: Consultar as informações da aplicação sem autenticação
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/api/info"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "info.json"
    E o campo "aplicacao" deve ser "residuos-esg"
    E o campo "versao" deve estar preenchido
    E o campo "ambiente" deve estar preenchido

  @positivo
  Cenário: Consultar a saúde da aplicação e do banco de dados
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/actuator/health"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "health.json"
    E o campo "status" deve ser "UP"

  @positivo @governanca
  Cenário: Consultar a documentação OpenAPI da API
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/v3/api-docs"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "openapi.json"
    E o campo "openapi" deve estar preenchido
