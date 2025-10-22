package com.lanqiDoctor.demo.ui.model;

/**
 * 权限项数据模型
 */
public class PermissionItem {
    
    /**
     * 权限类型
     */
    public enum Type {
        ESSENTIAL,      // 必需权限
        RECOMMENDED,    // 推荐权限
        OPTIONAL        // 可选权限
    }
    
    private String name;            // 权限名称
    private String description;     // 权限描述
    private String permission;      // 权限字符串
    private Type type;             // 权限类型
    private boolean granted;        // 是否已授予
    private boolean useXXPermissions; // 是否使用XXPermissions处理
    
    public PermissionItem(String name, String description, String permission, Type type, boolean useXXPermissions) {
        this.name = name;
        this.description = description;
        this.permission = permission;
        this.type = type;
        this.granted = false;
        this.useXXPermissions = useXXPermissions;
    }
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPermission() {
        return permission;
    }
    
    public void setPermission(String permission) {
        this.permission = permission;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public boolean isGranted() {
        return granted;
    }
    
    public void setGranted(boolean granted) {
        this.granted = granted;
    }
    
    public boolean isUseXXPermissions() {
        return useXXPermissions;
    }
    
    public void setUseXXPermissions(boolean useXXPermissions) {
        this.useXXPermissions = useXXPermissions;
    }
    
    /**
     * 获取权限类型的显示文本
     */
    public String getTypeText() {
        switch (type) {
            case ESSENTIAL:
                return "必需";
            case RECOMMENDED:
                return "推荐";
            case OPTIONAL:
                return "可选";
            default:
                return "";
        }
    }
    
    /**
     * 获取权限类型的颜色资源ID
     */
    public int getTypeColor() {
        switch (type) {
            case ESSENTIAL:
                return android.R.color.holo_red_dark;
            case RECOMMENDED:
                return android.R.color.holo_orange_dark;
            case OPTIONAL:
                return android.R.color.holo_blue_dark;
            default:
                return android.R.color.black;
        }
    }
}
