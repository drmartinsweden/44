package pl.ss.capstone.atmprotocol.atm;


import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.*;
import pl.ss.capstone.atmprotocol.common.Default;
import pl.ss.capstone.atmprotocol.common.parameter.*;
import pl.ss.capstone.atmprotocol.common.utils.ByteUtils;
import t.BankService;
import t.W;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Created by nulon on 05.10.15.
 */
public class ATM {

    private static DecimalFormat df = new DecimalFormat("0.##");

    public static void main(String[] args) {

        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(otherSymbols);

        String lastOp = "";
        Map<String, Argument> arguments = new HashMap<>();
        OptionSet options = null;
        String cardfile = "";

        long start = System.currentTimeMillis();
        try {
            //OptionParser parser = new OptionParser("s:i:p:c:a:n:d:w:g");
            OptionParser parser = new OptionParser();
            OptionSpec<String> optS = parser.accepts("s").withRequiredArg().ofType(String.class);
            OptionSpec<String> optI = parser.accepts("i").withRequiredArg().ofType(String.class);
            OptionSpec<String> optP = parser.accepts("p").withRequiredArg().ofType(String.class);
            OptionSpec<String> optC = parser.accepts("c").withRequiredArg().ofType(String.class);
            OptionSpec<String> optA = parser.accepts("a").withRequiredArg().ofType(String.class);
            OptionSpec<String> optN = parser.accepts("n").withRequiredArg().ofType(String.class);
            OptionSpec<String> optD = parser.accepts("d").withRequiredArg().ofType(String.class);
            OptionSpec<String> optW = parser.accepts("w").withRequiredArg().ofType(String.class);
            OptionSpec<Void> optG = parser.accepts("g");

            options = parser.parse(args);
            //// System.err.println("After parsing " + (System.currentTimeMillis() - start));
            if (options.nonOptionArguments().size() > 0) {
                throw new RuntimeException(String.format(String.format("Detected non option arguments")));
            }

            int specCount = 0;
            Argument arg = null;
            if (options.has(optS)){
                List<String> vals = options.valuesOf(optS);
                if (vals.size() > 1){
                    Runtime.getRuntime().halt(255);
                }
                arg = new FilenameArgument(new StringArgument("s", vals.get(0)));
                if (!arg.isValid()) {
                    // System.err.println("s not valid");
                    Runtime.getRuntime().halt(255);
                }
                arguments.put("s", arg);
                specCount++;
            }

            if (options.has(optI)){
                List<String> vals = options.valuesOf(optI);
                if (vals.size() > 1){
                    // System.err.println("to many optional arguments");
                    Runtime.getRuntime().halt(255);
                }
                arg = new IPArgument(new StringArgument("i", vals.get(0)));
                if (!arg.isValid()) {
                    // System.err.println("i not valid");
                    Runtime.getRuntime().halt(255);
                }
                arguments.put("i", arg);
                specCount++;
            }

            if (options.has(optP)){
                List<String> vals = options.valuesOf(optP);
                if (vals.size() > 1){
                    // System.err.println("to many arguments");
                    Runtime.getRuntime().halt(255);
                }
                arg = new PortArgument(new NumericArgument(new StringArgument("p", vals.get(0))));
                if (!arg.isValid()) {
                    // System.err.println("s not valid");
                    Runtime.getRuntime().halt(255);
                }
                arguments.put("p", arg);
                specCount++;
            }

            if (options.has(optC)){
                List<String> vals = options.valuesOf(optC);
                if (vals.size() > 1){
                    Runtime.getRuntime().halt(255);
                }
                arg = new FilenameArgument(new StringArgument("c", vals.get(0)));
                if (!arg.isValid())
                    Runtime.getRuntime().halt(255);
                arguments.put("c", arg);
                specCount++;
            }

            if (options.has(optA)){
                List<String> vals = options.valuesOf(optA);
                if (vals.size() > 1){
                    Runtime.getRuntime().halt(255);
                }
                arg = new AccountArgument(new StringArgument("a", vals.get(0)));
                if (!arg.isValid())
                    Runtime.getRuntime().halt(255);
                arguments.put("a", arg);
                specCount++;
            }

            if (options.has(optN)){
                List<String> vals = options.valuesOf(optN);
                if (vals.size() > 1){
                    Runtime.getRuntime().halt(255);
                }
                arg = new BalanceArgument(new StringArgument("n", vals.get(0)));
                if (!arg.isValid())
                    Runtime.getRuntime().halt(255);
                arguments.put("n", arg);
                specCount++;
            }

            if (options.has(optD)){
                List<String> vals = options.valuesOf(optD);
                if (vals.size() > 1){
                    Runtime.getRuntime().halt(255);
                }
                arg = new BalanceArgument(new StringArgument("d", vals.get(0)));
                if (!arg.isValid())
                    Runtime.getRuntime().halt(255);
                arguments.put("d", arg);
                specCount++;
            }

            if (options.has(optW)){
                List<String> vals = options.valuesOf(optW);
                if (vals.size() > 1){
                    Runtime.getRuntime().halt(255);
                }
                arg = new BalanceArgument(new StringArgument("w", vals.get(0)));
                if (!arg.isValid())
                    Runtime.getRuntime().halt(255);
                arguments.put("w", arg);
                specCount++;
            }

            if (options.has(optG)){
                specCount++;
                if (options.specs().size() != specCount){
                    Runtime.getRuntime().halt(255);
                }
                arg = new StringArgument("g", "");
                arguments.put("g", arg);
            }

            //// System.err.println("After options processing " + (System.currentTimeMillis() - start));

            if (!arguments.containsKey("a")) {
                //// System.err.println(String.format("Missing required 'a' argument"));
                Runtime.getRuntime().halt(255);
            }

            if (!arguments.containsKey("p")) {
                arguments.put("p", new PortArgument(new NumericArgument(new StringArgument("p", Default.PORT))));
            }

            if (!arguments.containsKey("i")) {
                arguments.put("i", new IPArgument(new StringArgument("i", Default.IP)));
            }

            if (!arguments.containsKey("s")) {
                arguments.put("s", new FilenameArgument(new StringArgument("s", Default.AUTH_FILE)));
            }

            if (!Files.isReadable(Paths.get(((FilenameArgument) arguments.get("s")).getValue()))) {
                //// System.err.println(String.format("Auth file %s cannot be accessed.", ((FilenameArgument) arguments.get("s")).getValue()));
                Runtime.getRuntime().halt(255);
            }

            if (!arguments.containsKey("c")) {
                arguments.put("c", new FilenameArgument(new StringArgument("c", String.format("%s%s", arguments.get("a").getValue(), Default.CARD_SFX))));
            }

            //// System.err.println("After arguments processing " + (System.currentTimeMillis() - start));

            int operationCount = 0;
            String ops = "ndwg";
            for (char c : ops.toCharArray()) {
                if (arguments.containsKey(String.valueOf(c))) {
                    operationCount++;
                    lastOp = String.valueOf(c);
                }
            }

            if (operationCount != 1) {
                //// System.err.println(String.format("Parameter count of bank operations is not equal exactly 1: %d", operationCount));
                Runtime.getRuntime().halt(255);
            }
            cardfile = ((FilenameArgument) arguments.get("c")).getValue();
            if ("n".equals(lastOp)) {
                if (Files.exists(Paths.get(cardfile))) {
                    // System.err.println(String.format("Card file %s already exists while invoking 'n' operation", cardfile));
                    Runtime.getRuntime().halt(255);
                }
            } else {
                if (!Files.isReadable(Paths.get(cardfile))) {
                    // System.err.println(String.format("Card file %s cannot be accessed.", cardfile));
                    Runtime.getRuntime().halt(255);
                }
            }

            //// System.err.println("After files checking " + (System.currentTimeMillis() - start));
        } catch (Exception ex) {
            // System.err.println(ex.getMessage());
            // ex.printStackTrace();
            Runtime.getRuntime().halt(255);
        }

        try {

            final String host = ((IPArgument) arguments.get("i")).getValue();
            final int port = ((PortArgument) arguments.get("p")).getValue().intValue();
            final String authfile = ((FilenameArgument) arguments.get("s")).getValue();
            final String card = cardfile;

            TSSLTransportFactory.TSSLTransportParameters params =
                    new TSSLTransportFactory.TSSLTransportParameters();
            params.setTrustStore(authfile, Default.STORE_PASS);

            TTransport transport = TSSLTransportFactory.getClientSocket(host, port, Default.TIMEOUT, params);

//            TTransport transport = new TSocket(host, port,Default.TIMEOUT);
//            transport.open();

            //// System.err.println("Socket opened " + (System.currentTimeMillis() - start));

            TProtocol protocol = new TCompactProtocol(transport);
            BankService.Client client = new BankService.Client(protocol);
            //// System.err.println("Client created " + (System.currentTimeMillis() - start));

            executeOperation(client, lastOp, cardfile, arguments);

        } catch(TTransportException e) {
            // System.err.println(e.getMessage());
            // e.printStackTrace();
            Runtime.getRuntime().halt(63);
        } catch (TException e) {
            // System.err.println(e.getMessage());
            // e.printStackTrace();
            Runtime.getRuntime().halt(63);
        } catch (IOException e) {
            // System.err.println(e.getMessage());
            // e.printStackTrace();
            Runtime.getRuntime().halt(255);
        } finally {
            //// System.err.println("Finished " + (System.currentTimeMillis() - start));
            Runtime.getRuntime().halt(0);
        }

    }

    private static void executeOperation(BankService.Client client, String operation, String cardfile, Map<String,Argument> args) throws TException, IOException {
        Long card = null;
        boolean success = false;
        W w = new W();
        w.setA(((AccountArgument) args.get("a")).getValue());
        w.setT(new Date().getTime());
        switch (operation) {
            case "n":
                w.setV(((BalanceArgument) args.get("n")).getValue().doubleValue());
                card = client.c(w);
                if (card < 0) Runtime.getRuntime().halt(255);
                try {
                    Files.write(Paths.get(cardfile), ByteUtils.longToBytes(card));
                } catch (IOException e) {
                    // System.err.println(e.getMessage());
                    Runtime.getRuntime().halt(255);
                }
                System.out.println(String.format("{\"account\":\"%s\",\"initial_balance\":%s}",w.getA(),df.format(w.getV())));
                break;
            case "d":
                card = ByteUtils.bytesToLong(Files.readAllBytes(Paths.get(cardfile)));
                w.setV(((BalanceArgument) args.get("d")).getValue().doubleValue());
                w.setC(card);
                success = client.d(w);
                if (!success) Runtime.getRuntime().halt(255);
                System.out.println(String.format("{\"account\":\"%s\",\"deposit\":%s}", w.getA(), df.format(w.getV())));
                break;
            case "w":
                card = ByteUtils.bytesToLong(Files.readAllBytes(Paths.get(cardfile)));
                w.setV(((BalanceArgument) args.get("w")).getValue().doubleValue());
                w.setC(card);
                success = client.w(w);
                if (!success) Runtime.getRuntime().halt(255);
                System.out.println(String.format("{\"account\":\"%s\",\"withdraw\":%s}", w.getA(), df.format(w.getV())));
                break;
            case "g":
                card = ByteUtils.bytesToLong(Files.readAllBytes(Paths.get(cardfile)));
                w.setC(card);
                double res = client.b(w);
                if (res < 0) Runtime.getRuntime().halt(255);
                System.out.println(String.format("{\"account\":\"%s\",\"balance\":%s}", w.getA(), df.format(res)));
                break;
        }
    }

}
