-- Usuarios iniciais (senhas com BCrypt): admin123 / operador123
INSERT INTO usuarios (nome, email, senha, perfil)
VALUES ('Administrador ESG', 'admin@esg.com', '$2a$10$36Bggwi5ki62Qlf26a.dHeauTKecotb3s1auCFNeLjXHmq0JVFD..', 'ADMIN');
INSERT INTO usuarios (nome, email, senha, perfil)
VALUES ('Operador de Coleta', 'operador@esg.com', '$2a$10$xKhDYihVehh4cP29mwUAxuE9Ck7GOaKTsBRsSLx0klJeueV9jmXj.', 'OPERADOR');

INSERT INTO pontos_coleta (nome, endereco, bairro, tipo_residuo, capacidade_kg)
VALUES ('Ecoponto Centro', 'Rua da Assembleia, 100', 'Centro', 'PLASTICO', 500.00);
INSERT INTO pontos_coleta (nome, endereco, bairro, tipo_residuo, capacidade_kg)
VALUES ('PEV Vidros Tijuca', 'Rua Conde de Bonfim, 450', 'Tijuca', 'VIDRO', 800.00);
INSERT INTO pontos_coleta (nome, endereco, bairro, tipo_residuo, capacidade_kg)
VALUES ('Coleta de Eletronicos Botafogo', 'Rua Voluntarios da Patria, 200', 'Botafogo', 'ELETRONICO', 300.00);

INSERT INTO coletas (ponto_coleta_id, data_coleta, quantidade_kg, status, observacao)
VALUES ((SELECT id FROM pontos_coleta WHERE nome = 'Ecoponto Centro'), DATE '2026-09-15', 120.50, 'CONCLUIDA', 'Coleta semanal');
INSERT INTO coletas (ponto_coleta_id, data_coleta, quantidade_kg, status, observacao)
VALUES ((SELECT id FROM pontos_coleta WHERE nome = 'PEV Vidros Tijuca'), DATE '2026-09-20', 340.00, 'CONCLUIDA', 'Conteiner cheio');
INSERT INTO coletas (ponto_coleta_id, data_coleta, quantidade_kg, status, observacao)
VALUES ((SELECT id FROM pontos_coleta WHERE nome = 'Coleta de Eletronicos Botafogo'), DATE '2026-10-20', 45.00, 'AGENDADA', 'Aguardando transportadora');
