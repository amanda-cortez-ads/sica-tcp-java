import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * Cliente do Sistema de Compartilhamento de Arquivos (SiCA).
 * Conecta-se ao servidor via Socket TCP e disponibiliza menu em linha
 * de comando para listagem, envio (Upload) e recebimento (Download) de arquivos.
 */
public class ClienteSiCA {
    private static final String HOST = "127.0.0.1";
    private static final int PORTA = 5000;
    private static final String DIRETORIO_CLIENTE = "cliente_arquivos";

    public static void main(String[] args) {
        File dir = new File(DIRETORIO_CLIENTE);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        System.out.println("[*] Tentando conectar ao servidor SiCA em " + HOST + ":" + PORTA + "...");

        try (
            Socket socket = new Socket(HOST, PORTA);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("[✔] Conexão TCP estabelecida com sucesso!");
            boolean executando = true;

            while (executando) {
                exibirMenu();
                System.out.print("Escolha uma opção (1-4): ");
                String opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        solicitarListagem(in, out);
                        break;
                    case "2":
                        System.out.print("Digite o nome do arquivo localizado em 'cliente_arquivos': ");
                        String nomeUpload = scanner.nextLine().trim();
                        realizarUpload(in, out, nomeUpload);
                        break;
                    case "3":
                        System.out.print("Digite o nome do arquivo que deseja baixar do servidor: ");
                        String nomeDownload = scanner.nextLine().trim();
                        realizarDownload(in, out, nomeDownload);
                        break;
                    case "4":
                        out.writeUTF("EXIT");
                        executando = false;
                        System.out.println("[*] Encerrando sessão do cliente...");
                        break;
                    default:
                        System.out.println("[!] Opção inválida.");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("[!] Não foi possível conectar ou manter sessão com o servidor: " + e.getMessage());
        }
    }

    private static void exibirMenu() {
        System.out.println("\n==================================");
        System.out.println("          MENU SICA (JAVA)       ");
        System.out.println("==================================");
        System.out.println("1. Listar arquivos no servidor");
        System.out.println("2. Enviar arquivo (Upload)");
        System.out.println("3. Baixar arquivo (Download)");
        System.out.println("4. Sair");
        System.out.println("==================================");
    }

    private static void solicitarListagem(DataInputStream in, DataOutputStream out) throws IOException {
        out.writeUTF("LIST");
        String lista = in.readUTF();
        System.out.println("\n--- ARQUIVOS DISPONÍVEIS NO SERVIDOR ---");
        System.out.println(lista);
        System.out.println("----------------------------------------");
    }

    private static void realizarUpload(DataInputStream in, DataOutputStream out, String nomeArquivo) throws IOException {
        File arq = new File(DIRETORIO_CLIENTE, nomeArquivo);
        if (!arq.exists()) {
            System.out.println("[!] Erro: O arquivo '" + nomeArquivo + "' não foi encontrado na pasta '" + DIRETORIO_CLIENTE + "'.");
            return;
        }

        out.writeUTF("UPLOAD");
        out.writeUTF(nomeArquivo);
        out.writeLong(arq.length());

        try (FileInputStream fis = new FileInputStream(arq)) {
            byte[] buffer = new byte[4096];
            int bytesLidos;
            while ((bytesLidos = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesLidos);
            }
        }
        out.flush();
        System.out.println("[✔] Upload do arquivo '" + nomeArquivo + "' concluído!");
    }

    private static void realizarDownload(DataInputStream in, DataOutputStream out, String nomeArquivo) throws IOException {
        out.writeUTF("DOWNLOAD");
        out.writeUTF(nomeArquivo);

        boolean existe = in.readBoolean();
        if (!existe) {
            System.out.println("[!] Erro: Arquivo '" + nomeArquivo + "' não existe no servidor.");
            return;
        }

        long tamanho = in.readLong();
        File destino = new File(DIRETORIO_CLIENTE, nomeArquivo);

        try (FileOutputStream fos = new FileOutputStream(destino)) {
            byte[] buffer = new byte[4096];
            long bytesLidosTotal = 0;
            int bytesLidos;

            while (bytesLidosTotal < tamanho && 
                  (bytesLidos = in.read(buffer, 0, (int) Math.min(buffer.length, tamanho - bytesLidosTotal))) != -1) {
                fos.write(buffer, 0, bytesLidos);
                bytesLidosTotal += bytesLidos;
            }
        }
        System.out.println("[✔] Download de '" + nomeArquivo + "' (" + tamanho + " bytes) concluído com sucesso!");
    }
}