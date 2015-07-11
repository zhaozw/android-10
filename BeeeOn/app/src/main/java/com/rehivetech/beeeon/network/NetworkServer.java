package com.rehivetech.beeeon.network;

import android.content.Context;

import com.rehivetech.beeeon.BuildConfig;
import com.rehivetech.beeeon.INameIdentifier;
import com.rehivetech.beeeon.R;

/**
 * @author Robyer
 */
public enum NetworkServer implements INameIdentifier {
	SERVER_ANT2_ALPHA("ant-2-alpha", "Ant-2 (alpha)", "ant-2.fit.vutbr.cz", 4566),
	SERVER_ANT2_BETA("ant-2-beta", "Ant-2 (beta)", "ant-2.fit.vutbr.cz", 4565),
	SERVER_IOTPRO("iotpro", "IOTPro (production)", "iotpro.fit.vutbr.cz", 4565);

	/** Name of default CA certificate located in assets */
	private static final String ASSETS_CA_CERT = "cacert.crt";

	/** Default CN value to be verified in server certificate */
	private static final String SERVER_CN_CERTIFICATE = "ant-2.fit.vutbr.cz";

	private final String mId;
	private final String mName;
	public final String address;
	public final int port;
	public final String certAssetsFilename;
	public final String certVerifyUrl;

	NetworkServer(String id, String name, String address, int port) {
		this(id, name, address, port, ASSETS_CA_CERT, SERVER_CN_CERTIFICATE);
	};

	NetworkServer(String id, String name, String address, int port, String certAssetsFilename, String certVerifyUrl) {
		mId = id;
		mName = name;

		this.address = address;
		this.port = port;
		this.certAssetsFilename = certAssetsFilename;
		this.certVerifyUrl = certVerifyUrl;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public String getName() {
		return mName;
	}

	public String getTranslatedName(Context context) {
		if (this == getDefaultServer()) {
			return String.format("%s %s", mName, context.getString(R.string.sufix_default));
		}
		return mName;
	}

	@Override
	public String toString() {
		return mName;
	}

	public static final NetworkServer getDefaultServer() {
		if (BuildConfig.BUILD_TYPE.equals("debug") || BuildConfig.BUILD_TYPE.equals("alpha")) {
			return SERVER_ANT2_ALPHA;
		} else if (BuildConfig.BUILD_TYPE.equals("beta_ant2")) {
			return SERVER_ANT2_BETA;
		} else {
			return SERVER_IOTPRO;
		}
	}
}