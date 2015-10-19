package pl.ss.capstone.atmprotocol.bank;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServerEventHandler;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;

import org.joda.time.DateTime;
import pl.ss.capstone.atmprotocol.common.Default;
import pl.ss.capstone.atmprotocol.common.parameter.*;
import t.BankService;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;
import java.util.*;


/**
 * Created by nulon on 05.10.15.
 */
public class Bank {

    private final int port;
    private final String authFile;

    public static BankServiceHandler handler;
    public static BankService.Processor processor;

    private KeyPair keypair;

    public Bank(int port, String authFile){
        this.port = port;
        this.authFile = authFile;
    }

    public String getAuthFile(){
        return authFile;
    }

    public boolean initializeAuthFile() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, CertificateException, KeyStoreException, java.security.cert.CertificateException {
        /*
        Path authFilePath = FileSystems.getDefault().getPath(authFile);
        if (Files.exists(authFilePath)){
            return false;
        }

        key = CryptoTool.generateKey();
        Files.write(authFilePath,key.getBytes());

        return true;*/
        Path authFilePath = FileSystems.getDefault().getPath(authFile);
        if (Files.exists(authFilePath)){
            return false;
        }
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        keygen.initialize(2048);
        keypair = keygen.generateKeyPair();

        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(new X500Name(Default.ISSUER), new BigInteger("1"), new DateTime().toDate() , new DateTime().plusYears(1).toDate(), new X500Name(Default.SUBJECT), SubjectPublicKeyInfo.getInstance(keypair.getPublic().getEncoded()));
        byte[] certBytes = certBuilder.build(new JCESigner(keypair.getPrivate(), "SHA256withRSA")).getEncoded();
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry(Default.CERT_ALIAS, certificate);
        ks.setKeyEntry(Default.KEY_ALIAS, keypair.getPrivate(), Default.STORE_PASS.toCharArray(), new Certificate[]{certificate});

        FileOutputStream fos = new FileOutputStream(authFile);
        ks.store(fos, Default.STORE_PASS.toCharArray());
        fos.close();

        return true;
    }

    public void startup() throws NoSuchAlgorithmException {
        handler = new BankServiceHandler(keypair);
        processor = new BankService.Processor(handler);
        try {
            TSSLTransportFactory.TSSLTransportParameters params = new TSSLTransportFactory.TSSLTransportParameters();
            params.setKeyStore(authFile, Default.STORE_PASS, null, null);
            TServerTransport serverTransport = TSSLTransportFactory.getServerSocket(port, Default.TIMEOUT, null, params);

            //TServerTransport serverTransport = new TServerSocket(port,Default.TIMEOUT);
            TServer server = new ModifiedTSimpleServer(new TServer.Args(serverTransport)
//                    .transportFactory(new TFramedTransport.Factory())
                    .protocolFactory(new TCompactProtocol.Factory())
                    .processor(processor));
            server.serve();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        Map<String, Argument> arguments = new HashMap<>();
        OptionSet options = null;
        try {
            OptionParser parser = new OptionParser("s:p:");
            options = parser.parse(args);

            if (options.nonOptionArguments().size() > 0){
                throw new RuntimeException(String.format(String.format("Detected non option arguments")));
            }

            List<OptionSpec<?>> optionsList = options.specs();
            for(OptionSpec spec : optionsList){
                List<String> names = spec.options();
                for(String optionName : names){
                    if (arguments.containsKey(optionName)){
                        throw new RuntimeException(String.format(String.format("Duplicate option: %s", optionName)));
                    }else{
                        switch(optionName){
                            case "s":
                                arguments.put(optionName,new FilenameArgument(new StringArgument(optionName,(String)options.valueOf(optionName))));
                                break;
                            case "p":
                                arguments.put(optionName,new PortArgument(new NumericArgument(new StringArgument(optionName,(String)options.valueOf(optionName)))));
                                break;
                        }
                    }
                }
            }
            if (!arguments.containsKey("s")){
                arguments.put("s",new FilenameArgument(new StringArgument("s", Default.AUTH_FILE)));
            }
            if (!arguments.containsKey("p")){
                arguments.put("p", new PortArgument(new NumericArgument(new StringArgument("p", Default.PORT))));
            }

            for(Argument arg : arguments.values()){
                if (!arg.isValid()){
                    throw new RuntimeException(String.format("Argument %s is invalid %s",arg.getName(),arg.getValue()));
                }else{
//                    System.err.println(String.format("Argument %s passed with value %s", arg.getName(), arg.getValue()));
                }
            }

            Bank bank = new Bank(((PortArgument)arguments.get("p")).getValue().intValue(),((FilenameArgument)arguments.get("s")).getValue());
            if (!bank.initializeAuthFile()){
                throw new RuntimeException(String.format("Error during auth file initialization %s",bank.getAuthFile()));
            }else{
                System.out.println("created");
            }

            bank.startup();

        }catch(Exception ex){
            System.err.println(ex.getMessage());
            ex.printStackTrace();
            System.exit(255);
        }

    }
}
