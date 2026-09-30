import java.io.*;
import java.net.*;

/**
 * Servidor do Sistema de Compartilhamento de Arquivos (SiCA).
 * Utiliza Sockets TCP para ouvir conexões de clientes, listar arquivos
 * armazenados no diretório do servidor e realizar operações de Upload e Download.
 */
public class ServidorSiCA {
    private static final int PORTA = 5000;
    private static final String DIRETORIO_SERVIDOR = "servidor_arquivos";

    public static void main(String[] args) {
        File dir = new File(DIRETORIO_SERVIDOR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println("[*] Servidor SiCA (Java) rodando na porta " + PORTA);
        System.out.println("[*] Diretório de armazenamento: " + dir.getAbsolutePath());

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("\n[+] Cliente conectado: " + socket.getInetAddress().getHostAddress());
                tratarCliente(socket);
            }
        } catch (IOException e) {
            System.err.println("[!] Erro no servidor: " + e.getMessage());
        }
    }

    private static void tratarCliente(Socket socket) {
        try (
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream())
        ) {
            boolean conectado = true;

            while (conectado) {
                String comando = in.readUTF();

                switch (comando) {
                    case "LIST":
                        listarArquivos(out);
                        break;
                    case "UPLOAD":
                        String nomeUpload = in.readUTF();
                        long tamanhoUpload = in.readLong();
                        receberArquivo(in, nomeUpload, tamanhoUpload);
                        break;
                    case "DOWNLOAD":
                        String nomeDownload = in.readUTF();
                        enviarArquivo(out, nomeDownload);
                        break;
                    case "EXIT":
                        System.out.println("[-] Cliente solicitou encerramento.");
                        conectado = false;
                        break;
                    default:
                        out.writeUTF("ERROR: Comando inválido.");
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("[-] Conexão com o cliente finalizada.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("[!] Erro ao fechar socket: " + e.getMessage());
            }
        }
    }

    private static void listarArquivos(DataOutputStream out) throws IOException {
        File dir = new File(DIRETORIO_SERVIDOR);
        File[] arquivos = dir.listFiles();
        StringBuilder lista = new StringBuilder();

        if (arquivos != null && arquivos.length > 0) {
            for (File f : arquivos) {
                if (f.isFile()) {
                    lista.append(f.getName()).append(" (").append(f.length()).append(" bytes)\n");
                }
            }
        } else {
            lista.append("Nenhum arquivo encontrado no servidor.");
        }

        out.writeUTF(lista.toString());
    }

    private static void receberArquivo(DataInputStream in, String nomeArquivo, long tamanho) throws IOException {
        File arquivo = new File(DIRETORIO_SERVIDOR, nomeArquivo);
        try (FileOutputStream fos = new FileOutputStream(arquivo)) {
            byte[] buffer = new byte[4096];
            long bytesLidosTotal = 0;
            int bytesLidos;

            while (bytesLidosTotal < tamanho && 
                  (bytesLidos = in.read(buffer, 0, (int) Math.min(buffer.length, tamanho - bytesLidosTotal))) != -1) {
                fos.write(buffer, 0, bytesLidos);
                bytesLidosTotal += bytesLidos;
            }
        }
        System.out.println("[✔] Upload concluído: " + nomeArquivo + " (" + tamanho + " bytes)");
    }

    private static void enviarArquivo(DataOutputStream out, String nomeArquivo) throws IOException {
        File arquivo = new File(DIRETORIO_SERVIDOR, nomeArquivo);

        if (!arquivo.exists()) {
            out.writeBoolean(false);
            return;
        }

        out.writeBoolean(true);
        out.writeLong(arquivo.length());

        try (FileInputStream fis = new FileInputStream(arquivo)) {
            byte[] buffer = new byte[4096];
            int bytesLidos;
            while ((bytesLidos = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesLidos);
            }
        }
        out.flush();
        System.out.println("[✔] Download de '" + nomeArquivo + "' enviado ao cliente.");
    }
}