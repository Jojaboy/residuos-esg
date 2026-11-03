# language: pt
@api @coletas
Funcionalidade: Rastreamento das coletas de resíduos
  Como operador da coleta seletiva
  Quero agendar, alterar e concluir coletas
  Para rastrear o caminho dos resíduos até a reciclagem

  Contexto:
    Dado que a API está no ar

  @positivo
  Cenário: Listar as coletas
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição GET para "/api/coletas"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "coletas-lista.json"
    E a resposta deve ser uma lista com pelo menos 3 itens

  @positivo @governanca
  Cenário: Listar somente as coletas de um ponto
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "12.50" kg nesse ponto
    Quando envio uma requisição GET para "/api/coletas?pontoColetaId={pontoId}"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "coletas-lista.json"
    E a lista deve conter a coleta criada
    E todas as coletas devem pertencer ao ponto criado

  @positivo @esg
  Cenário: Agendar uma coleta
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    Quando envio uma requisição POST para "/api/coletas" com o corpo:
      """
      {
        "pontoColetaId": {pontoId},
        "dataColeta": "2026-11-20",
        "quantidadeKg": 42.50,
        "observacao": "Coleta semanal"
      }
      """
    Então o status da resposta deve ser 201
    E o corpo da resposta deve seguir o contrato "coleta.json"
    E o cabeçalho "Location" deve estar preenchido
    E o campo "status" deve ser "AGENDADA"
    E o campo "dataColeta" deve ser "2026-11-20"
    E o campo "quantidadeKg" deve ser o número "42.50"
    E o campo "tipoResiduo" deve ser "METAL"
    E o campo "pontoColetaId" deve ser o identificador do ponto criado

  @positivo
  Cenário: Agendar uma coleta sem observação
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    Quando envio uma requisição POST para "/api/coletas" com o corpo:
      """
      {
        "pontoColetaId": {pontoId},
        "dataColeta": "2026-11-21",
        "quantidadeKg": 5
      }
      """
    Então o status da resposta deve ser 201
    E o corpo da resposta deve seguir o contrato "coleta.json"

  @negativo
  Cenário: Agendar uma coleta para um ponto inexistente
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição POST para "/api/coletas" com o corpo:
      """
      {
        "pontoColetaId": 999999,
        "dataColeta": "2026-11-20",
        "quantidadeKg": 42.50
      }
      """
    Então o status da resposta deve ser 404
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "nao encontrado"

  @negativo
  Esquema do Cenário: Agendar uma coleta com dados inválidos
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    Quando envio uma requisição POST para "/api/coletas" com o corpo:
      """
      {
        "pontoColetaId": {pontoId},
        "dataColeta": <data>,
        "quantidadeKg": <quantidade>
      }
      """
    Então o status da resposta deve ser 400
    E o corpo da resposta deve seguir o contrato "erro.json"
    E os detalhes do erro devem citar o campo "<campo>"

    Exemplos:
      | data         | quantidade | campo        |
      | "2026-11-20" | 0          | quantidadeKg |
      | "2026-11-20" | null       | quantidadeKg |
      | null         | 10         | dataColeta   |

  @negativo @seguranca
  Cenário: Agendar uma coleta sem autenticação
    Dado que não estou autenticado
    Quando envio uma requisição POST para "/api/coletas" com o corpo:
      """
      {"pontoColetaId": 1, "dataColeta": "2026-11-20", "quantidadeKg": 10}
      """
    Então o status da resposta deve ser 401

  @positivo
  Cenário: Buscar uma coleta pelo identificador
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "20.00" kg nesse ponto
    Quando envio uma requisição GET para "/api/coletas/{coletaId}"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "coleta.json"
    E o campo "status" deve ser "AGENDADA"

  @negativo
  Cenário: Buscar uma coleta inexistente
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição GET para "/api/coletas/999999"
    Então o status da resposta deve ser 404
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "nao encontrada"

  @negativo
  Cenário: Excluir uma coleta inexistente
    Dado que estou autenticado como "ADMIN"
    Quando envio uma requisição DELETE para "/api/coletas/999999"
    Então o status da resposta deve ser 404
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "nao encontrada"

  @positivo
  Cenário: Alterar uma coleta agendada
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "20.00" kg nesse ponto
    Quando envio uma requisição PUT para "/api/coletas/{coletaId}" com o corpo:
      """
      {
        "pontoColetaId": {pontoId},
        "dataColeta": "2026-12-01",
        "quantidadeKg": 33.30,
        "observacao": "Data remarcada"
      }
      """
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "coleta.json"
    E o campo "dataColeta" deve ser "2026-12-01"
    E o campo "quantidadeKg" deve ser o número "33.30"
    E o campo "observacao" deve ser "Data remarcada"

  @negativo @governanca
  Cenário: Uma coleta concluída não pode ser alterada
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "20.00" kg nesse ponto
    E que essa coleta já foi concluída
    Quando envio uma requisição PUT para "/api/coletas/{coletaId}" com o corpo:
      """
      {
        "pontoColetaId": {pontoId},
        "dataColeta": "2026-12-01",
        "quantidadeKg": 999.00
      }
      """
    Então o status da resposta deve ser 409
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "Somente coletas AGENDADAS podem ser alteradas"

  @positivo @esg
  Cenário: Concluir uma coleta agendada
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "20.00" kg nesse ponto
    Quando envio uma requisição PATCH para "/api/coletas/{coletaId}/concluir"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "coleta.json"
    E o campo "status" deve ser "CONCLUIDA"

  @negativo @governanca
  Cenário: Uma coleta não pode ser concluída duas vezes
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "20.00" kg nesse ponto
    E que essa coleta já foi concluída
    Quando envio uma requisição PATCH para "/api/coletas/{coletaId}/concluir"
    Então o status da resposta deve ser 409
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "Somente coletas AGENDADAS podem ser concluidas"

  @negativo
  Cenário: Concluir uma coleta inexistente
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição PATCH para "/api/coletas/999999/concluir"
    Então o status da resposta deve ser 404
    E o corpo da resposta deve seguir o contrato "erro.json"

  @positivo @governanca
  Cenário: Administrador exclui uma coleta
    Dado que estou autenticado como "ADMIN"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "20.00" kg nesse ponto
    Quando envio uma requisição DELETE para "/api/coletas/{coletaId}"
    Então o status da resposta deve ser 204
    E o corpo da resposta deve estar vazio
    E o registro não deve mais existir em "/api/coletas/{coletaId}"

  @negativo @governanca
  Cenário: Operador não tem permissão para excluir uma coleta
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "20.00" kg nesse ponto
    Quando envio uma requisição DELETE para "/api/coletas/{coletaId}"
    Então o status da resposta deve ser 403
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "Acesso negado"
