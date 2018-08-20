package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs A proxy filesystem to serve files from. See the PCDPFilesystem
     *           class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
            final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {

            Socket accepted = socket.accept();

            Thread t = new Thread(() -> {
                try(InputStream is = accepted.getInputStream()) {
                    try (InputStreamReader r = new InputStreamReader(is)) {
                        try (BufferedReader br = new BufferedReader(r)) {
                            String response = new String();
                            String input = br.readLine();
                            if (input == null || input.length() == 0) {
                                sendBadRequest(accepted);
                            } else {
                                String[] inputParts = input.split(" ");
                                if (inputParts.length < 3) {
                                    sendBadRequest(accepted);
                                } else {
                                    if (!inputParts[0].equals("GET")) {
                                        sendBadRequest(accepted);
                                    } else {
                                        String path = inputParts[1];
                                        String fileContents = fs.readFile(new PCDPPath(path));
                                        if (fileContents != null)
                                            sendOk(accepted, fileContents);
                                        else
                                            sendNotFound(accepted);
                                    }
                                }
                            }

                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
        }
    }

    private boolean sendOk(Socket socket, String contents) throws IOException {
        String response = buildResponse(200, "OK", contents);
        writeResponse(socket, response);
        return true;
    }

    private boolean sendNotFound(Socket socket) throws IOException {
        String response = buildResponse(404, "Not Found", null);
        writeResponse(socket, response);
        return true;
    }

    private boolean sendBadRequest(Socket socket) throws IOException {
        String response = buildResponse(400, "Bad Request", null);
        writeResponse(socket, response);
        return true;
    }

    private boolean writeResponse(Socket socket, String response) throws IOException {
        try (OutputStream os = socket.getOutputStream()) {
            try (OutputStreamWriter osw = new OutputStreamWriter(os)) {
                osw.write(response);
            }
        }
        return true;
    }

    private String buildResponse(int code, String codeDesc, String contents) {
        StringBuilder sbResponse = new StringBuilder();
        sbResponse.append("HTTP/1.0 " + code + " " + codeDesc).append("\r\n");
        sbResponse.append("Server: FileServer").append("\r\n");
        sbResponse.append("\r\n");
        if (contents != null)
            sbResponse.append(contents);
        return sbResponse.toString();
    }
}
