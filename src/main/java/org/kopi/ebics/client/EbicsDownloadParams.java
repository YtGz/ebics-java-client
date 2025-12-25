package org.kopi.ebics.client;

/**
 * Parameters for EBICS BTD (Business Transaction Download) requests.
 * Used with EBICS 3.0 (H005) to download files using Service descriptors.
 *
 * @param serviceName  The service name (e.g., "STD", "MKT")
 * @param scope        The service scope (e.g., "BIL" for Switzerland billing)
 * @param option       The service option (e.g., "CH003ZMD")
 * @param messageName  The message name (e.g., "smd", "tmd")
 * @param containerType The container type (e.g., "ZIP", "XML")
 */
public record EbicsDownloadParams(
    String serviceName,
    String scope,
    String option,
    String messageName,
    String containerType
) {
    /**
     * Returns the file extension based on the container type.
     *
     * @return file extension including the dot (e.g., ".zip", ".xml")
     */
    public String getFileExtension() {
        if ("ZIP".equalsIgnoreCase(containerType)) {
            return ".zip";
        }
        return ".xml";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("BTD[");
        sb.append(serviceName);
        if (scope != null) {
            sb.append("/").append(scope);
        }
        if (option != null) {
            sb.append("/").append(option);
        }
        if (messageName != null) {
            sb.append(" (").append(messageName).append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
