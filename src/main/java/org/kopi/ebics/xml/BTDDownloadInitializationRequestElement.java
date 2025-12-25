/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.kopi.ebics.xml;

import java.util.Calendar;

import org.kopi.ebics.client.EbicsDownloadParams;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.schema.h005.BTDOrderParamsDocument;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Body;
import org.kopi.ebics.schema.h005.EbicsRequestDocument.EbicsRequest.Header;
import org.kopi.ebics.schema.h005.MutableHeaderType;
import org.kopi.ebics.schema.h005.StaticHeaderOrderDetailsType;
import org.kopi.ebics.schema.h005.StaticHeaderType;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Authentication;
import org.kopi.ebics.schema.h005.StaticHeaderType.BankPubKeyDigests.Encryption;
import org.kopi.ebics.schema.h005.StaticHeaderType.Product;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;

/**
 * The <code>BTDDownloadInitializationRequestElement</code> handles EBICS 3.0 (H005)
 * BTD (Business Transaction Download) initialization requests.
 *
 * <p>BTD is used to download files using Service descriptors (BTF format) instead of
 * the legacy 3-letter order types.
 */
public class BTDDownloadInitializationRequestElement extends InitializationRequestElement {

    private final EbicsDownloadParams downloadParams;

    /**
     * Constructs a new BTD download initialization request.
     *
     * @param session the current ebics session
     * @param downloadParams the BTD download parameters (service name, scope, etc.)
     */
    public BTDDownloadInitializationRequestElement(EbicsSession session,
                                                   EbicsDownloadParams downloadParams) {
        super(session, OrderType.FDL, generateName(OrderType.FDL));
        this.downloadParams = downloadParams;
    }

    @Override
    public void buildInitialization() throws EbicsException {
        EbicsRequest request;
        Header header;
        Body body;
        MutableHeaderType mutable;
        StaticHeaderType xstatic;
        Product product;
        BankPubKeyDigests bankPubKeyDigests;
        Authentication authentication;
        Encryption encryption;
        StaticHeaderOrderDetailsType orderDetails;

        mutable = EbicsXmlFactory.createMutableHeaderType("Initialisation", null);
        product = EbicsXmlFactory.createProduct(session.getProduct().getLanguage(),
            session.getProduct().getName());
        authentication = EbicsXmlFactory.createAuthentication(
            session.getConfiguration().getAuthenticationVersion(),
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(session.getUser().getPartner().getBank().getX002Digest()));
        encryption = EbicsXmlFactory.createEncryption(
            session.getConfiguration().getEncryptionVersion(),
            "http://www.w3.org/2001/04/xmlenc#sha256",
            decodeHex(session.getUser().getPartner().getBank().getE002Digest()));
        bankPubKeyDigests = EbicsXmlFactory.createBankPubKeyDigests(authentication, encryption);

        // Create BTD order params
        var btdParams = EbicsXmlFactory.createBTDParams(
            downloadParams.serviceName(),
            downloadParams.scope(),
            downloadParams.option(),
            downloadParams.messageName(),
            downloadParams.containerType()
        );

        var adminOrderType = StaticHeaderOrderDetailsType.AdminOrderType.Factory.newInstance();
        adminOrderType.setStringValue("BTD");

        String orderId = session.getConfiguration().isDownloadOrderIdEnabled()
            ? session.getUser().getPartner().nextOrderId()
            : null;

        orderDetails = EbicsXmlFactory.createStaticHeaderOrderDetailsType(
            orderId,
            adminOrderType,
            btdParams,
            BTDOrderParamsDocument.type);

        xstatic = EbicsXmlFactory.createStaticHeaderType(
            session.getBankID(),
            nonce,
            session.getUser().getPartner().getPartnerId(),
            product,
            session.getUser().getSecurityMedium(),
            session.getUser().getUserId(),
            Calendar.getInstance(),
            orderDetails,
            bankPubKeyDigests);

        header = EbicsXmlFactory.createEbicsRequestHeader(true, mutable, xstatic);
        body = EbicsXmlFactory.createEbicsRequestBody();
        request = EbicsXmlFactory.createEbicsRequest(
            session.getConfiguration().getRevision(),
            session.getConfiguration().getVersion(),
            header,
            body);
        document = EbicsXmlFactory.createEbicsRequestDocument(request);
    }

    @Override
    public String getType() {
        return "BTD";
    }

    private static final long serialVersionUID = 1L;
}
