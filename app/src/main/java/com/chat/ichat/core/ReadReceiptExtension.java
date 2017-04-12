package com.chat.ichat.core;


import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;

import java.util.List;
import java.util.Map;

/**
 * Created by vidhun on 26/11/16.
 */

public class ReadReceiptExtension implements ExtensionElement {
    public static final String NAMESPACE = "urn:xmpp:read";
    public static final String ELEMENT = "read";
    public static final String ATTRIBUTE_RECEIPT_ID = "receipt_id";

    private String lastMessageReceiptId;

    public ReadReceiptExtension(String lastMessageReceiptId) {
        this.lastMessageReceiptId = lastMessageReceiptId;
    }

    public String getLastMessageReceiptId() {
        return lastMessageReceiptId;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public CharSequence toXML() {
        return "<read xmlns='" + NAMESPACE + "' "+ATTRIBUTE_RECEIPT_ID+"='" + lastMessageReceiptId + "'/>";
    }

    public static class Provider extends EmbeddedExtensionProvider<ReadReceiptExtension> {
        @Override
        protected ReadReceiptExtension createReturnExtension(String currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content) {
            ReadReceiptExtension readReceiptExtension = new ReadReceiptExtension(attributeMap.get(ATTRIBUTE_RECEIPT_ID));
            return readReceiptExtension;
        }
    }
}