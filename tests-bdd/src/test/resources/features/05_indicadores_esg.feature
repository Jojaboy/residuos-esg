# language: pt
@api @indicadores @esg
Funcionalidade: Indicadores ESG de reciclagem
  Como gestor ambiental da cidade
  Quero acompanhar indicadores calculados a partir das coletas
  Para reportar resultados de reciclagem com dados confiáveis

  Contexto:
    Dado que a API está no ar

  @positivo
  Cenário: Consultar os indicadores de reciclagem
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição GET para "/api/indicadores"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "indicadores.json"

  @negativo @seguranca
  Cenário: Consultar os indicadores sem autenticação
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/api/indicadores"
    Então o status da resposta deve ser 401

  @positivo @governanca
  Cenário: Coleta concluída entra no total de quilos reciclados
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "75.25" kg nesse ponto
    E que registrei os indicadores atuais
    Quando envio uma requisição PATCH para "/api/coletas/{coletaId}/concluir"
    E consulto novamente os indicadores
    Então o indicador "totalKgReciclado" deve ter aumentado em "75.25"
    E o indicador "coletasConcluidas" deve ter aumentado em "1"
    E o indicador "coletasAgendadas" deve ter aumentado em "-1"
    E o indicador "totalColetas" deve ter permanecido igual

  @negativo @governanca
  Cenário: Coleta apenas agendada não conta como resíduo reciclado
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que registrei os indicadores atuais
    E que existe uma coleta agendada de "500.00" kg nesse ponto
    Quando consulto novamente os indicadores
    Então o indicador "totalKgReciclado" deve ter permanecido igual
    E o indicador "coletasAgendadas" deve ter aumentado em "1"
    E o indicador "totalColetas" deve ter aumentado em "1"
