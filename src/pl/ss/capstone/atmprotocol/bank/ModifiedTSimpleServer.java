package pl.ss.capstone.atmprotocol.bank;

/**
 * Created by nulon on 12.10.15.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.server.ServerContext;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class ModifiedTSimpleServer extends TServer {
    //private static final Logger LOGGER = LoggerFactory.getLogger(ModifiedTSimpleServer.class.getName());
    private volatile boolean stopped_ = false;

    public ModifiedTSimpleServer(AbstractServerArgs args) {
        super(args);
    }

    public void serve() {
        try {
            this.serverTransport_.listen();
        } catch (TTransportException var9) {
            //LOGGER.error("Error occurred during listening.", var9);
            return;
        }

        if(this.eventHandler_ != null) {
            this.eventHandler_.preServe();
        }

        this.setServing(true);

        while(!this.stopped_) {
            TTransport client = null;
            TProcessor processor = null;
            TTransport inputTransport = null;
            TTransport outputTransport = null;
            TProtocol inputProtocol = null;
            TProtocol outputProtocol = null;
            ServerContext connectionContext = null;

            try {
                client = this.serverTransport_.accept();
                if (client != null) {
                    processor = this.processorFactory_.getProcessor(client);
                    inputTransport = this.inputTransportFactory_.getTransport(client);
                    outputTransport = this.outputTransportFactory_.getTransport(client);
                    inputProtocol = this.inputProtocolFactory_.getProtocol(inputTransport);
                    outputProtocol = this.outputProtocolFactory_.getProtocol(outputTransport);
                    if (this.eventHandler_ != null) {
                        connectionContext = this.eventHandler_.createContext(inputProtocol, outputProtocol);
                    }

                    do {
                        if (this.eventHandler_ != null) {
                            this.eventHandler_.processContext(connectionContext, inputTransport, outputTransport);
                        }
                    } while (processor.process(inputProtocol, outputProtocol));
                }
            }catch(TProtocolException ex){
                System.out.println("protocol_error");
            }catch (TTransportException var10) {
                if (var10.getMessage() != null && (
                        var10.getMessage().contains("SocketTimeoutException")
                        || var10.getMessage().toLowerCase().contains("ssl")
                )){
                    System.out.println("protocol_error");
                }
            } catch (TException var11) {
                if(!this.stopped_) {
                    //LOGGER.error("Thrift error occurred during processing of message.", var11);
                }
            } catch (Exception var12) {
                var12.printStackTrace(System.err);
                if(!this.stopped_) {
                    //LOGGER.error("Error occurred during processing of message.", var12);
                }
            }

            if(this.eventHandler_ != null) {
                this.eventHandler_.deleteContext(connectionContext, inputProtocol, outputProtocol);
            }

            if(inputTransport != null) {
                inputTransport.close();
            }

            if(outputTransport != null) {
                outputTransport.close();
            }
        }

        this.setServing(false);
    }

    public void stop() {
        this.stopped_ = true;
        this.serverTransport_.interrupt();
    }
}
