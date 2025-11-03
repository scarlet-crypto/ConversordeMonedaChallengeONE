import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    private static final String API_KEY = "3852b9a371133d0d0bb2a42d";
    private static final String MONEDA_BASE = "USD";
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/latest/" + MONEDA_BASE;

    private enum Moneda {
        USD, ARS, BRL, COP
    }

    private static class ApiResult {
        @SerializedName("result")
        String result;
        @SerializedName("conversion_rates")
        Map<String, Double> conversionRates;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<Moneda, Double> tasasActuales;

        try {
            System.out.println("Cargando tasas de cambio en tiempo real...");
            tasasActuales = obtenerTasasDeCambio();
            System.out.println("Tasas cargadas con éxito. Base: " + MONEDA_BASE + "\n");
        } catch (Exception e) {
            System.err.println("ERROR al obtener las tasas de cambio de la API:");
            System.err.println(e.getMessage());
            System.err.println("El programa no puede continuar sin tasas de cambio.");
            return;
        }

        int opcion = 0;
        while (opcion != 7) {
            mostrarMenu();
            try {
                opcion = Integer.parseInt(scanner.nextLine());
                ejecutarOpcion(opcion, scanner, tasasActuales);
            } catch (NumberFormatException e) {
                System.out.println("Entrada no válida. Por favor, ingrese un número del menú.");
                opcion = 0;
            } catch (Exception e) {
                System.out.println("Ocurrió un error inesperado: " + e.getMessage());
            }
        }
        System.out.println("Gracias por usar el conversor de monedas. ¡Hasta pronto!");
        scanner.close();
    }

    private static Map<Moneda, Double> obtenerTasasDeCambio() throws IOException, InterruptedException, IllegalStateException {
        if (API_KEY.equalsIgnoreCase("TU_CLAVE_API_AQUI")) {
            throw new IllegalStateException("Por favor, reemplaza 'TU_CLAVE_API_AQUI' con una clave de API válida.");
        }

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Fallo la conexión con la API. Código de estado: " + response.statusCode());
        }

        String jsonResponse = response.body();
        Gson gson = new Gson();
        ApiResult apiResult = gson.fromJson(jsonResponse, ApiResult.class);

        if (!"success".equals(apiResult.result)) {
            throw new IOException("La API devolvió un resultado no exitoso.");
        }

        Map<Moneda, Double> tasas = new HashMap<>();
        for (Moneda moneda : Moneda.values()) {
            if (apiResult.conversionRates.containsKey(moneda.name())) {
                tasas.put(moneda, apiResult.conversionRates.get(moneda.name()));
            }
        }
        return tasas;
    }

    private static void mostrarMenu() {
        System.out.println("==================================================");
        System.out.println("              CONVERSOR DE MONEDAS                ");
        System.out.println("==================================================");
        System.out.println("1) Dólar (USD) => Peso Argentino (ARS)");
        System.out.println("2) Peso Argentino (ARS) => Dólar (USD)");
        System.out.println("3) Dólar (USD) => Real Brasileño (BRL)");
        System.out.println("4) Real Brasileño (BRL) => Dólar (USD)");
        System.out.println("5) Dólar (USD) => Peso Colombiano (COP)");
        System.out.println("6) Peso Colombiano (COP) => Dólar (USD)");
        System.out.println("7) Salir");
        System.out.println("==================================================");
        System.out.print("Seleccione una opción: ");
    }

    private static void ejecutarOpcion(int opcion, Scanner scanner, Map<Moneda, Double> tasas) {
        if (opcion >= 1 && opcion <= 6) {
            Moneda origen;
            Moneda destino;

            switch (opcion) {
                case 1 -> { origen = Moneda.USD; destino = Moneda.ARS; }
                case 2 -> { origen = Moneda.ARS; destino = Moneda.USD; }
                case 3 -> { origen = Moneda.USD; destino = Moneda.BRL; }
                case 4 -> { origen = Moneda.BRL; destino = Moneda.USD; }
                case 5 -> { origen = Moneda.USD; destino = Moneda.COP; }
                case 6 -> { origen = Moneda.COP; destino = Moneda.USD; }
                default -> { return; }
            }

            System.out.print("Ingrese la cantidad de " + origen.name() + " a convertir: ");
            double cantidad = Double.parseDouble(scanner.nextLine());

            if (cantidad <= 0) {
                System.out.println("La cantidad a convertir debe ser un valor positivo.");
                return;
            }

            double resultado = convertirA(cantidad, origen, destino, tasas);

            System.out.printf("Resultado: %.2f %s equivalen a %.2f %s\n", cantidad, origen.name(), resultado, destino.name());
        } else if (opcion != 7) {
            System.out.println("Opción inválida. Por favor, ingrese un número del 1 al 7.");
        }
    }

    private static double convertirA(double cantidad, Moneda origen, Moneda destino, Map<Moneda, Double> tasas) {

        double tasaOrigen = tasas.getOrDefault(origen, 0.0);
        double tasaDestino = tasas.getOrDefault(destino, 0.0);

        if (tasaOrigen == 0.0 || tasaDestino == 0.0) {
            throw new IllegalStateException("Tasa de cambio no encontrada para las monedas seleccionadas.");
        }

        return cantidad * (tasaDestino / tasaOrigen);
    }
}
