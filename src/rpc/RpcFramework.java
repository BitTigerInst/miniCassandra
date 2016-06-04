package rpc;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import util.Check;
import util.Debug;

class RPC_Entry{
	Class<?> interfaceClass;
	String host;
	int port;

	public RPC_Entry(Class<?> interfaceClass, String host, int port) {
		this.interfaceClass = interfaceClass;
		this.host = host;
		this.port = port;
	}
}

/**
 * RpcFramework
 * 
 */
public class RpcFramework {
	
	public static <T> T is_contain(Class<T> interfaceClass, final String host, final int port) {
		for(Entry<RPC_Entry, Object> e: maps.entrySet()) {
			RPC_Entry entry = e.getKey();
			if(entry.interfaceClass.toString().equals(interfaceClass.toString())  && entry.host.equals(host) && entry.port==port) {
				return (T) e.getValue();
			}
		}
		return null;
	}

	private boolean running;
	static ConcurrentHashMap<RPC_Entry, Object> maps = new ConcurrentHashMap<>();

	public RpcFramework(boolean running) {
		this.running = running;
	}

    /**
     * 暴露服务
     * 
     * @param service 服务实现
     * @param port 服务端口
     * @throws Exception
     */
    public void export(final Object service, int port) throws Exception {
        if (service == null)
            throw new IllegalArgumentException("service instance == null");
        if (port <= 0 || port > 65535)
            throw new IllegalArgumentException("Invalid port " + port);
        Debug.debug("Export service " + service.getClass().getName() + " on port " + port);
        ServerSocket server = new ServerSocket(port);
        while(running) {
            try {
            	//a blocking method
                final Socket socket = server.accept();
                if(running) {
	                new Thread(new Runnable() {
	                    @Override
	                    public void run() {
	                        try {
	                            try {
	                                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
	                                try {
	                                    String methodName = input.readUTF();
	                                    Class<?>[] parameterTypes = (Class<?>[])input.readObject();
	                                    Object[] arguments = (Object[])input.readObject();
	                                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
	                                    try {
	                                        Method method = service.getClass().getMethod(methodName, parameterTypes);
	                                        Object result = method.invoke(service, arguments);
	                                        output.writeObject(result);
	                                    } catch (Throwable t) {
	                                        output.writeObject(t);
	                                    } finally {
	                                        output.close();
	                                    }
	                                } finally {
	                                    input.close();
	                                }
	                            } finally {
	                                socket.close();
	                            }
	                        } catch (Exception e) {
	                            e.printStackTrace();
	                        }
	                    }
	                }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 引用服务
     * 
     * @param <T> 接口泛型
     * @param interfaceClass 接口类型
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return 远程服务
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T refer(final Class<T> interfaceClass, final String host, final int port) throws Exception {
        Check.check_null(interfaceClass, "null interface class");
        Check.check_bool(interfaceClass.isInterface(), "invalid interfaceClass");
        Check.check_null(host, "invalid host");
        Check.check_bool(host.length()!=0, "invalide host length");
        Check.check_bool(port>0, "invalid port");
        Check.check_bool(port<=65535, "invalid port");

        synchronized(maps) {
	        T result = is_contain(interfaceClass, host, port);
	        if(result==null) {
	        	result = (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] {interfaceClass}, new InvocationHandler() {
	                public Object invoke(Object proxy, Method method, Object[] arguments) throws Throwable {
	                    Socket socket = new Socket(host, port);
	                    try {
	                        ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
	                        try {
	                            output.writeUTF(method.getName());
	                            output.writeObject(method.getParameterTypes());
	                            output.writeObject(arguments);
	                            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
	                            try {
	                                Object result = input.readObject();
	                                if (result instanceof Throwable) {
	                                    throw (Throwable) result;
	                                }
	                                return result;
	                            } finally {
	                                input.close();
	                            }
	                        } finally {
	                            output.close();
	                        }
	                    } finally {
	                        socket.close();
	                    }
	                }
	            });
	        	RPC_Entry new_rpc_entry = new RPC_Entry(interfaceClass, host, port);
	        	maps.put(new_rpc_entry, result);
	            Debug.debug("Create remote service " + interfaceClass.getName() + " from server " + host + ":" + port);
	        }
	        else {
	            Debug.debug("Get cached Remote service " + interfaceClass.getName() + " from server " + host + ":" + port);
	        }
	        return result;
        }
    }

    public void destroy() {
    	this.running = false;
    }
}