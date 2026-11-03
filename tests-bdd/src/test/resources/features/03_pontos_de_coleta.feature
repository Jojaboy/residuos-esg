# language: pt
@api @pontos-coleta
Funcionalidade: Gestão de pontos de coleta seletiva
  Como operador da coleta seletiva
  Quero manter os pontos de descarte da cidade atualizados
  Para que os resíduos tenham a destinação correta

  Contexto:
    Dado que a API está no ar

  @positivo
  Cenário: Listar os pontos de coleta
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição GET para "/api/pontos-coleta"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "pontos-coleta-lista.json"
    E a resposta deve ser uma lista com pelo menos 3 itens

  @positivo @esg
  Esquema do Cenário: Filtrar os pontos de coleta por tipo de resíduo
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição GET para "/api/pontos-coleta?tipo=<tipo>"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "pontos-coleta-lista.json"
    E todos os itens devem ter o campo "tipoResiduo" igual a "<tipo>"

    Exemplos:
      | tipo       |
      | PLASTICO   |
      | VIDRO      |
      | ELETRONICO |

  @negativo
  Cenário: Filtrar por um tipo de resíduo que não existe
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição GET para "/api/pontos-coleta?tipo=RADIOATIVO"
    Então o status da resposta deve ser 400
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "Parametro invalido: tipo"

  @positivo @esg
  Cenário: Cadastrar um novo ponto de coleta
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição POST para "/api/pontos-coleta" com o corpo:
      """
      {
        "nome": "Ecoponto Copacabana",
        "endereco": "Avenida Atlantica, 1000",
        "bairro": "Copacabana",
        "tipoResiduo": "METAL",
        "capacidadeKg": 250.50
      }
      """
    Então o status da resposta deve ser 201
    E o corpo da resposta deve seguir o contrato "ponto-coleta.json"
    E o cabeçalho "Location" deve estar preenchido
    E o campo "nome" deve ser "Ecoponto Copacabana"
    E o campo "tipoResiduo" deve ser "METAL"
    E o campo "capacidadeKg" deve ser o número "250.50"

  @negativo
  Esquema do Cenário: Cadastrar um ponto de coleta com dados inválidos
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição POST para "/api/pontos-coleta" com o corpo:
      """
      {
        "nome": "<nome>",
        "endereco": "Rua dos Testes, 100",
        "bairro": "Centro",
        "tipoResiduo": "PAPEL",
        "capacidadeKg": <capacidade>
      }
      """
    Então o status da resposta deve ser 400
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "Dados invalidos"
    E os detalhes do erro devem citar o campo "<campo>"

    Exemplos:
      | nome          | capacidade | campo        |
      |               | 100        | nome         |
      | Ponto Valido  | 0          | capacidadeKg |
      | Ponto Valido  | null       | capacidadeKg |

  @negativo
  Cenário: Cadastrar um ponto de coleta com tipo de resíduo inexistente
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição POST para "/api/pontos-coleta" com o corpo:
      """
      {
        "nome": "Ponto Invalido",
        "endereco": "Rua dos Testes, 100",
        "bairro": "Centro",
        "tipoResiduo": "RADIOATIVO",
        "capacidadeKg": 100
      }
      """
    Então o status da resposta deve ser 400
    E o corpo da resposta deve seguir o contrato "erro.json"

  @positivo
  Cenário: Buscar um ponto de coleta pelo identificador
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    Quando envio uma requisição GET para "/api/pontos-coleta/{pontoId}"
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "ponto-coleta.json"
    E o campo "id" deve ser o identificador do ponto criado

  @negativo
  Cenário: Atualizar um ponto de coleta inexistente
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição PUT para "/api/pontos-coleta/999999" com o corpo:
      """
      {
        "nome": "Ponto Inexistente",
        "endereco": "Rua dos Testes, 100",
        "bairro": "Centro",
        "tipoResiduo": "PAPEL",
        "capacidadeKg": 100
      }
      """
    Então o status da resposta deve ser 404
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "nao encontrado"

  @negativo
  Cenário: Excluir um ponto de coleta inexistente
    Dado que estou autenticado como "ADMIN"
    Quando envio uma requisição DELETE para "/api/pontos-coleta/999999"
    Então o status da resposta deve ser 404
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "nao encontrado"

  @negativo
  Cenário: Buscar um ponto de coleta inexistente
    Dado que estou autenticado como "OPERADOR"
    Quando envio uma requisição GET para "/api/pontos-coleta/999999"
    Então o status da resposta deve ser 404
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "nao encontrado"

  @positivo
  Cenário: Atualizar um ponto de coleta
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    Quando envio uma requisição PUT para "/api/pontos-coleta/{pontoId}" com o corpo:
      """
      {
        "nome": "Ponto Atualizado",
        "endereco": "Rua Nova, 200",
        "bairro": "Tijuca",
        "tipoResiduo": "ORGANICO",
        "capacidadeKg": 900
      }
      """
    Então o status da resposta deve ser 200
    E o corpo da resposta deve seguir o contrato "ponto-coleta.json"
    E o campo "nome" deve ser "Ponto Atualizado"
    E o campo "tipoResiduo" deve ser "ORGANICO"
    E o campo "capacidadeKg" deve ser o número "900"

  @positivo @governanca
  Cenário: Administrador exclui um ponto de coleta sem coletas
    Dado que estou autenticado como "ADMIN"
    E que existe um ponto de coleta cadastrado
    Quando envio uma requisição DELETE para "/api/pontos-coleta/{pontoId}"
    Então o status da resposta deve ser 204
    E o corpo da resposta deve estar vazio
    E o registro não deve mais existir em "/api/pontos-coleta/{pontoId}"

  @negativo @governanca
  Cenário: Operador não tem permissão para excluir um ponto de coleta
    Dado que estou autenticado como "OPERADOR"
    E que existe um ponto de coleta cadastrado
    Quando envio uma requisição DELETE para "/api/pontos-coleta/{pontoId}"
    Então o status da resposta deve ser 403
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "Acesso negado"

  @negativo @governanca
  Cenário: Ponto de coleta com histórico não pode ser excluído
    Dado que estou autenticado como "ADMIN"
    E que existe um ponto de coleta cadastrado
    E que existe uma coleta agendada de "10.00" kg nesse ponto
    Quando envio uma requisição DELETE para "/api/pontos-coleta/{pontoId}"
    Então o status da resposta deve ser 409
    E o corpo da resposta deve seguir o contrato "erro.json"
    E a mensagem de erro deve conter "nao pode ser excluido"

  @negativo @seguranca
  Cenário: Acessar os pontos de coleta sem autenticação
    Dado que não estou autenticado
    Quando envio uma requisição GET para "/api/pontos-coleta"
    Então o status da resposta deve ser 401

  @negativo @seguranca
  Cenário: Acessar os pontos de coleta com um token inválido
    Dado que estou usando um token inválido
    Quando envio uma requisição GET para "/api/pontos-coleta"
    Então o status da resposta deve ser 401
