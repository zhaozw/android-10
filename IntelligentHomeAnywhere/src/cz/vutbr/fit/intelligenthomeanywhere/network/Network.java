package cz.vutbr.fit.intelligenthomeanywhere.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import cz.vutbr.fit.intelligenthomeanywhere.activity.LoginActivity;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.Adapter;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.ParsedMessage;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlCreator;
import cz.vutbr.fit.intelligenthomeanywhere.adapter.parser.XmlParsers;
import cz.vutbr.fit.intelligenthomeanywhere.exception.CommunicationException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NoConnectionException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotImplementedException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotRegAException;
import cz.vutbr.fit.intelligenthomeanywhere.exception.NotRegBException;

/**
 * Network service that handles communication with server.
 * 
 * @author ThinkDeep
 * @author Robyer
 */
public class Network {
	
	public static final String SIGNIN = "signin";
	public static final String SIGNUP = "signup";
	public static final String FALSE = "false";
	public static final String TRUE = "true";
	public static final String NOTREGA = "notreg-a";
	public static final String NOTREGB = "notreg-b";
	public static final String READY = "ready";
	public static final String RESIGN = "resign";
	public static final String XML = "xml";
	
	/**
	 * Name of CA certificate located in assets
	 */
	private static final String ASSEST_CA_CERT = "cacert.crt";

	/**
	 * Alias (tag) for CA certificate
	 */
	private static final String ALIAS_CA_CERT = "ca";

	/**
	 * Address or hostName of server
	 */
	private static final String SERVER_ADDR = "ant-2.fit.vutbr.cz";

	/**
	 * Port of server
	 */
	private static final int SERVER_PORT = 4565;

	/**
	 * CN value to be verified in server certificate
	 */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";
	
	private static Context mContext;
	private static int mSessionId;
	
	/**
	 * Constructor.
	 * @param context
	 */
	public Network(Context context) {
		mContext = context;
	}
	
	/**
	 * Static function for sending data to server via TLS protocol using own
	 * TrustManger to be able to trust self-signed certificates. CA certificated
	 * must be located in assets folder. If no exception is thrown, it returns
	 * server response.
	 * 
	 * @param appContext
	 *            Application context to get CA certificate from assets
	 * @param request
	 *            Request to server to be sent
	 * @return Response from server
	 * @throws IOException
	 *             Can't read CA certificate from assets, can't read InputStream
	 *             or can't write OutputStream.
	 * @throws CertificateException
	 *             Unknown certificate format (default X.509), can't generate CA
	 *             certificate (it shouldn't occur)
	 * @throws KeyStoreException
	 *             Bad type of KeyStore, can't set CA certificate to KeyStore
	 * @throws NoSuchAlgorithmException
	 *             Unknown SSL/TLS protocol or unknown TrustManager algorithm
	 *             (it shouldn't occur)
	 * @throws KeyManagementException
	 *             general exception, thrown to indicate an exception during
	 *             processing an operation concerning key management
	 * @throws UnknownHostException
	 *             *IMPORTANT* Server address or hostName wasn't not found
	 * @throws SSLHandshakeException
	 *             *IMPORTANT* TLS handshake failed
	 */
	private static String startCommunication(/*Context appContext,*/ String request)
			throws IOException, CertificateException, KeyStoreException,
					NoSuchAlgorithmException, KeyManagementException,
					UnknownHostException, SSLHandshakeException
	{

		/*
		 * opening CA certificate from assets
		 */
		InputStream inStreamCertTmp = null;

		inStreamCertTmp = /*appContext*/mContext.getAssets().open(ASSEST_CA_CERT);

		InputStream inStreamCert = new BufferedInputStream(inStreamCertTmp);
		Certificate ca;
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			ca = cf.generateCertificate(inStreamCert);
		} finally {
			inStreamCert.close();
		}
		// Create a KeyStore containing our trusted CAs
		String keyStoreType = KeyStore.getDefaultType();
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		keyStore.load(null, null);
		keyStore.setCertificateEntry(ALIAS_CA_CERT, ca);

		// Create a TrustManager that trusts the CAs in our KeyStore
		String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
		TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
		tmf.init(keyStore);

		// Create an SSLContext that uses our TrustManager
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, tmf.getTrustManagers(), null);

		// Open SSLSocket directly to server
		SSLSocket socket = (SSLSocket) sslContext.getSocketFactory()
				.createSocket(SERVER_ADDR, SERVER_PORT);

		HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
		SSLSession s = socket.getSession();

		// Verify that the certificate hostName
		// This is due to lack of SNI support in the current SSLSocket.
		if (!hv.verify(SERVER_CN_CERTIFICATE, s)) {
			throw new SSLHandshakeException("Expected CN value:"
					+ SERVER_CN_CERTIFICATE + ", found " + s.getPeerPrincipal());
		}

		// At this point SSLSocket performed certificate verification and
		// we have performed hostName verification, so it is safe to proceed.
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream()));
		BufferedReader r = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		w.write(request, 0, request.length());
		w.flush();

		StringBuilder response = new StringBuilder();
		String actRecieved = null;
		while ((actRecieved = r.readLine()) != null) {
			response.append(actRecieved);
		}

		// close socket, writer and reader
		w.close();
		r.close();
		socket.close();

		// return server response
		return response.toString();
	}

	/**
	 * Checks if Internet connection is available.
	 * @return true if available, false otherwise
	 * @throws NotImplementedException
	 */
	public boolean isAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/**
	 * Method signIn user given by its email to server, BUT before calling must call GetGoogleAuth to get googleToken in it and Init ActualUser
	 * @param userEmail of current user
	 * @return ActualUser
	 * @throws NoConnectionException if there is no Internet connection
	 * @throws CommunicationException if there is some problem with certificate, timeout, or other communication problem
	 * @throws NotRegAException if this user is not registered on server and on server is NO FREE ADAPTER (without its lord)
	 * @throws NotRegBException if this user is not registered on the server but there is FREE ADAPTER
	 */
	public ActualUser signIn(String userEmail) throws NoConnectionException, CommunicationException, NotRegAException, NotRegBException{
		
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String googleToken;
			
			do{
				googleToken = GetGoogleAuth.getGetGoogleAuth().getToken();
			}while(googleToken.length() < 1);
			
			String messageToSend = XmlCreator.createSignIn(userEmail, googleToken);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getSessionId() != 0 && msg.getState().equals(TRUE) && ((String)msg.data).equals(SIGNIN)){
			Log.d("IHA - Network", msg.getState());
			
			ActualUser aUser = ActualUser.getActualUser();
			aUser.setSessionId(Integer.toString(msg.getSessionId()));
			mSessionId = msg.getSessionId();
			
			return aUser;
		}
		if(msg.getState().equals(NOTREGA)){
			throw new NotRegAException();
		}
		if(msg.getState().equals(NOTREGB)){
			throw new NotRegBException();
		}
			
		return null;
	}
	
	/**
	 * Method sign user up to adapter with its email, serial number of adapter (user is in role superuser)
	 * @param email of registering user
	 * @param serialNumber number of adapter to register
	 * @param SessionId if is ID == 0 then needed google token, then the user is switch to work with new adapter, otherwise work with old
	 * @return true if everything goes well, false otherwise
	 * @throws CommunicationException
	 * @throws NoConnectionException
	 */
	public boolean signUp(String email, String serialNumber, int SessionId) throws CommunicationException, NoConnectionException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String googleToken;
			
			do{
				googleToken = GetGoogleAuth.getGetGoogleAuth().getToken();
			}while(googleToken.length() < 1);
			
			String messageToSend = XmlCreator.createSignUp(email, Integer.toString(SessionId), googleToken, serialNumber);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getSessionId() != 0 && msg.getState().equals(TRUE) && ((String)msg.data).equals(SIGNUP)){
			Log.d("IHA - Network", msg.getState());
			
			ActualUser aUser = ActualUser.getActualUser();
			aUser.setSessionId(Integer.toString(msg.getSessionId()));
			
			return true;
		}else //FIXME: do something with false additional info (why not register)
			return false;
	}
	
	/**
	 * Method ask for list of adapters. User has to be sign in before
	 * @return list of adapters or null
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Adapter> getAdapters() throws NoConnectionException, CommunicationException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createGetAdapters(mSessionId);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(READY)){
			Log.d("IHA - Network", msg.getState());
			
			return (ArrayList<Adapter>) msg.data;
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return getAdapters();
		}else
			//FIXME: do something with false additional info (why not register)
			return null;
	}
	
	/**
	 * Method as for whole adapter data
	 * @param adapterId of wanted adapter
	 * @return Adapter
	 * @throws NoConnectionException
	 * @throws CommunicationException
	 */
	public Adapter init(String adapterId) throws NoConnectionException, CommunicationException{
		if(!isAvailable())
			throw new NoConnectionException();
		
		ParsedMessage msg;
		
		try {
			String messageToSend = XmlCreator.createInit(Integer.toString(mSessionId), adapterId);
			
			String result = startCommunication(messageToSend);
			
			Log.d("IHA - Network", result);
			
			msg = XmlParsers.parseCommunication(result, false);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new CommunicationException(e);
		}
		
		if(msg.getState().equals(XML)){
			Log.d("IHA - Network", msg.getState());
			
			return (Adapter) msg.data;
		}else if(msg.getState().equals(RESIGN)){
			//TODO: maybe use diffrenD way to resign, case stopping of thread, manage this after implement in the controler
			try {
				GetGoogleAuth.getGetGoogleAuth().execute();
			} catch (Exception e) {
				e.printStackTrace();
				String tmp = null;
				new GetGoogleAuth(new LoginActivity(), tmp).execute();
				//return null;
			}
			signIn(ActualUser.getActualUser().getEmail());
			return init(adapterId);
		}else
			//FIXME: do something with false additional info (why not register)
			return null;
	}
	
	
	
}
