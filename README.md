# SiCA — Sistema de Compartilhamento de Arquivos (TCP Sockets em Java)

Este projeto implementa uma aplicação cliente-servidor para transferência e gerenciamento de arquivos em rede utilizando **Sockets TCP** na linguagem Java.

# Descrição do Funcionamento
A aplicação utiliza a arquitetura Cliente-Servidor orientada à conexão (TCP) sobre a API de Sockets nativa do Java (java.net.ServerSocket e java.net.Socket).

Estabelecimento de Conexão: O servidor aguarda conexões ativas na porta 5000. Ao conectar, o cliente inicia uma sessão de troca de mensagens codificadas em UTF e transmissões de dados binários em blocos (chunks de 4KB).

Delimitação de Pacotes (Framing): Para evitar corrupção de dados ou travamentos na leitura do socket, todas as transmissões de arquivos utilizam um cabeçalho inicial que informa o nome e o tamanho total do arquivo em bytes (long), garantindo que o receptor leia exatamente a quantidade esperada de bytes.

Persistência Local:

O servidor armazena e lê os arquivos no diretório ./servidor_arquivos/.

O cliente lê arquivos para upload e salva os downloads no diretório ./cliente_arquivos/.

# Protocolo de aplicação ( COMANDO - ORIGEM - RESPOSTA )
LIST - Cliente -> Servidor - O servidor lê a pasta e retorna uma string contendo a relação de arquivos disponíveis.
UPLOAD - Cliente -> Servidor - O cliente envia o nome do arquivo, o tamanho em bytes e transmite o fluxo de dados binários.
DOWNLOAD - Cliente -> Servidor - O servidor valida a existência do arquivo, retorna confirmação booleana, o tamanho e inicia a transmissão.
EXIT - Cliente -> Servidor - Encerra a sessão TCP e fecha os sockets de comunicação.

# Pré-requisitos (onde executar)
Java Development Kit (JDK 8 ou superior) ou IDE (NetBeans / Eclipse / VS Code).
