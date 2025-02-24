package online.codeisfun.plugins.serializers;

class CIFSubType {
    private CIFField.Type type;
    private String objectClassName;

    public String getType() {
        switch (type) {
            case LIST:
            case MAP:
                throw new RuntimeException("subtype not supported");
            case OBJECT:
                return objectClassName+"Proto";
            default:
                return type.getProtoType();
        }
    }

    public static CIFSubType fromProtoType(CIFField.Type type) {
        CIFSubType CIFSubType = new CIFSubType();
        CIFSubType.type = type;
        CIFSubType.objectClassName = type.getProtoType();
        return CIFSubType;
    }

    public static CIFSubType fromProtoType(CIFField.Type type, String objectClassName) {
        CIFSubType CIFSubType = new CIFSubType();
        CIFSubType.type = type;
        CIFSubType.objectClassName = objectClassName;
        return CIFSubType;
    }
}
