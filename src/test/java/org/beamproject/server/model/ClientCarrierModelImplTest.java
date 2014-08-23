/*
 * Copyright (C) 2013, 2014 beamproject.org
 *
 * This file is part of beam-server.
 *
 * beam-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beam-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beamproject.server.model;

import java.util.logging.Logger;
import org.beamproject.common.message.Message;
import static org.beamproject.common.message.Field.Cnt.*;
import static org.beamproject.common.message.Field.Cnt.Typ.*;
import org.beamproject.common.Participant;
import org.beamproject.common.Server;
import org.beamproject.common.User;
import org.beamproject.common.carrier.ClientCarrier;
import org.beamproject.common.crypto.CryptoPacker;
import org.beamproject.common.crypto.CryptoPackerPool;
import org.beamproject.common.crypto.CryptoPackerPoolFactory;
import static org.beamproject.common.crypto.EccKeyPairGenerator.fromPublicKey;
import org.beamproject.common.crypto.HandshakeResponder;
import org.beamproject.server.ExecutorFake;
import org.beamproject.server.util.HandshakeStorage;
import org.beamproject.server.util.SessionStorage;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

public class ClientCarrierModelImplTest {

    private final CryptoPacker PACKER = new CryptoPacker();
    private final User USER = User.generate();
    private final Participant USER_WITH_ONLY_PUBLIC_KEY = new Participant(fromPublicKey(USER.getPublicKeyAsBytes()));
    private final Server SERVER = Server.generate();
    private MainModel mainModel;
    private ClientCarrier carrier;
    private HandshakeStorage<HandshakeResponder> handshakeStorage;
    private SessionStorage sessionStorage;
    private ClientCarrierModelImpl model;
    private Message message;

    @Before
    public void setUp() {
        mainModel = createMock(MainModel.class);
        carrier = createMock(ClientCarrier.class);
        handshakeStorage = new HandshakeStorage<>();
        sessionStorage = new SessionStorage();

        model = new ClientCarrierModelImpl(mainModel,
                new ExecutorFake(),
                carrier,
                getPackerPool(),
                handshakeStorage,
                sessionStorage);
        model.log = Logger.getGlobal();
    }

    private CryptoPackerPool getPackerPool() {
        CryptoPackerPoolFactory factory = new CryptoPackerPoolFactory();
        return new CryptoPackerPool(factory);
    }

    @Test
    public void testConsumeMessageOnUnknwonType() {
        message = new Message();
        message.setRecipient(USER_WITH_ONLY_PUBLIC_KEY);
        expect(mainModel.getServer()).andReturn(SERVER);
        replay(mainModel, carrier);

        model.consumeMessage(encrypt(message));

        // nothing should happen
        verify(mainModel, carrier);
    }

    @Test
    public void testConsumeMessageOnWrongRecipient() {
        message = new Message();
        message.setRecipient(USER_WITH_ONLY_PUBLIC_KEY);
        expect(mainModel.getServer()).andReturn(SERVER);
        replay(mainModel, carrier);

        model.consumeMessage(encrypt(message));

        // nothing should happen
        verify(mainModel, carrier);
    }

    @Test
    public void testEncryptAndSend() {
        final Message message = new Message(FORWARD, USER);
        message.putContent(MSG, "my message");

        carrier.deliverMessage(anyObject(byte[].class), anyObject(Participant.class));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            @Override
            public Object answer() {
                byte[] ciphertext = (byte[]) getCurrentArguments()[0];
                Participant recipient = (Participant) getCurrentArguments()[1];
                Message plaintext = PACKER.decryptAndUnpack(ciphertext, recipient);

                assertEquals(USER, message.getRecipient());
                assertSame(USER, recipient);
                assertArrayEquals("my message".getBytes(), plaintext.getContent(MSG));
                return null;
            }
        });
        replay(carrier);

        model.encryptAndSend(message);

        verify(carrier);
    }

    private byte[] encrypt(Message message) {
        CryptoPacker packer = new CryptoPacker();
        return packer.packAndEncrypt(message);
    }

}
