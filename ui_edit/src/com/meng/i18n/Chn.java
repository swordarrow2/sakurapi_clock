package com.meng.i18n;

import java.util.*;

/**
 * Chinese language implementation (Simplified Chinese).
 */
public class Chn extends I18nContent {

    // Static language info
    public static String NAME = "简体中文";
    public static String CODE = "zh";

    private final Map<String, String> keyInfoMap;

    public Chn() {
        keyInfoMap = new LinkedHashMap<>();
        initKeyInfo();
    }

    private void initKeyInfo() {
        keyInfoMap.put("cfg.name", "字符串: 主题显示名称");
        keyInfoMap.put("cfg.delay", "整数: 动画帧延迟（每帧切换背景的帧数）");
        keyInfoMap.put("background.file", "文件名: 静态背景图片 (jpg/png/bmp)。当设置 background.dir 时禁用（动态背景）");
        keyInfoMap.put("background.dir", "目录: 动态背景文件夹，包含多张图片。设置后 background.file 将被禁用");
        keyInfoMap.put("hardware.screen_width", "整数: 屏幕宽度（像素）");
        keyInfoMap.put("hardware.screen_height", "整数: 屏幕高度（像素）");
        keyInfoMap.put("gui.show_cursor", "布尔值: 0=隐藏光标, 1=显示光标");
        keyInfoMap.put("performance.max_fps", "整数: 最大 FPS 限制。使用 0 表示无限制");
        keyInfoMap.put("time.font", "文件名: 字体文件 (ttf/ttc/otf)，支持 FreeType 字体");
        keyInfoMap.put("time.size", "整数: 文字大小（像素）");
        keyInfoMap.put("time.color", "颜色: 0xRRGGBBAA(十六进制), RRGGBBAA(十六进制), 0xRRGGBB(十六进制), RRGGBB(十六进制), R,G,B,A(十进制) 或 R,G,B(十进制)");
        keyInfoMap.put("time.colorOutline", "颜色: 格式同 color");
        keyInfoMap.put("time.outlineSize", "整数: 描边大小（像素）");
        keyInfoMap.put("time.x", "整数: 屏幕上的 X 坐标位置");
        keyInfoMap.put("time.y", "整数: 屏幕上的 Y 坐标位置");
        keyInfoMap.put("date.font", "文件名: 字体文件 (ttf/ttc/otf)，支持 FreeType 字体");
        keyInfoMap.put("date.size", "整数: 文字大小（像素）");
        keyInfoMap.put("date.color", "颜色: 格式同 time.color");
        keyInfoMap.put("date.colorOutline", "颜色: 格式同 color");
        keyInfoMap.put("date.outlineSize", "整数: 描边大小（像素）");
        keyInfoMap.put("date.x", "整数: 屏幕上的 X 坐标位置");
        keyInfoMap.put("date.y", "整数: 屏幕上的 Y 坐标位置");
        keyInfoMap.put("fps.font", "文件名: 字体文件 (ttf/ttc/otf)，支持 FreeType 字体");
        keyInfoMap.put("fps.size", "整数: 文字大小（像素）");
        keyInfoMap.put("fps.color", "颜色: 格式同 time.color");
        keyInfoMap.put("fps.colorOutline", "颜色: 格式同 color");
        keyInfoMap.put("fps.outlineSize", "整数: 描边大小（像素）");
        keyInfoMap.put("fps.x", "整数: 屏幕上的 X 坐标位置");
        keyInfoMap.put("fps.y", "整数: 屏幕上的 Y 坐标位置");
        keyInfoMap.put("cpu_state.font", "文件名: 字体文件 (ttf/ttc/otf)，支持 FreeType 字体");
        keyInfoMap.put("cpu_state.size", "整数: 文字大小（像素）");
        keyInfoMap.put("cpu_state.color", "颜色: 格式同 time.color");
        keyInfoMap.put("cpu_state.colorOutline", "颜色: 格式同 color");
        keyInfoMap.put("cpu_state.outlineSize", "整数: 描边大小（像素）");
        keyInfoMap.put("cpu_state.x", "整数: 屏幕上的 X 坐标位置");
        keyInfoMap.put("cpu_state.y", "整数: 屏幕上的 Y 坐标位置");
        keyInfoMap.put("memory_state.font", "文件名: 字体文件 (ttf/ttc/otf)，支持 FreeType 字体");
        keyInfoMap.put("memory_state.size", "整数: 文字大小（像素）");
        keyInfoMap.put("memory_state.color", "颜色: 格式同 time.color");
        keyInfoMap.put("memory_state.colorOutline", "颜色: 格式同 color");
        keyInfoMap.put("memory_state.outlineSize", "整数: 描边大小（像素）");
        keyInfoMap.put("memory_state.x", "整数: 屏幕上的 X 坐标位置");
        keyInfoMap.put("memory_state.y", "整数: 屏幕上的 Y 坐标位置");
        keyInfoMap.put("storage_state.font", "文件名: 字体文件 (ttf/ttc/otf)，支持 FreeType 字体");
        keyInfoMap.put("storage_state.size", "整数: 文字大小（像素）");
        keyInfoMap.put("storage_state.color", "颜色: 格式同 time.color");
        keyInfoMap.put("storage_state.colorOutline", "颜色: 格式同 color");
        keyInfoMap.put("storage_state.outlineSize", "整数: 描边大小（像素）");
        keyInfoMap.put("storage_state.x", "整数: 屏幕上的 X 坐标位置");
        keyInfoMap.put("storage_state.y", "整数: 屏幕上的 Y 坐标位置");
    }

    @Override
    public String getEditorKeyInfo(String key) {
        return keyInfoMap.getOrDefault(key, "");
    }

    // ========== Menu Bar ==========

    @Override
    public String getMenuTheme() { return "主题"; }

    @Override
    public String getMenuEdit() { return "编辑"; }

    @Override
    public String getMenuLanguage() { return "语言"; }

    @Override
    public String getMenuHelp() { return "帮助"; }

    @Override
    public String getMenuNewTheme() { return "新建主题"; }

    @Override
    public String getMenuPackTheme() { return "打包主题 (.tar)"; }

    @Override
    public String getMenuUnpackTar() { return "解包 .tar"; }

    @Override
    public String getMenuExit() { return "退出"; }

    @Override
    public String getMenuSaveConfig() { return "保存配置"; }

    @Override
    public String getMenuOpenFolder() { return "打开主题目录"; }

    @Override
    public String getMenuRefreshList() { return "刷新主题列表"; }

    @Override
    public String getMenuAbout() { return "关于"; }

    // ========== Config Editor Panel ==========

    @Override
    public String getConfigEditorTitle() { return "配置编辑器"; }

    @Override
    public String getColumnKey() { return "键"; }

    @Override
    public String getColumnValue() { return "值"; }

    @Override
    public String getStatusNoConfig() { return "未加载配置"; }

    @Override
    public String getStatusLoadedPrefix() { return "已加载: "; }

    @Override
    public String getConfigKeyHelpTitle() { return "配置键帮助"; }

    @Override
    public String getAddKeyTitle() { return "添加键"; }

    @Override
    public String getAddKeyMessage() {
        return "选择要添加的键:\n" +
                "  \u2022 前缀选项（如 \"cfg\"）添加该类型下的所有键\n" +
                "  \u2022 单个键只添加这一个键";
    }

    @Override
    public String getNothingToAddTitle() { return "无可添加"; }

    @Override
    public String getNothingToAddMessage() { return "所有可用键都已添加。"; }

    @Override
    public String getBrowseButtonText() { return "浏览..."; }

    @Override
    public String getChooseFileTitle() { return "选择文件"; }

    @Override
    public String getNoThemeTitle() { return "无主题"; }

    @Override
    public String getNoThemeMessage() { return "未加载主题。请先选择一个主题。"; }

    @Override
    public String getDynamicBgDisabledHint() { return "动态背景 - 文件选择已禁用"; }

    // ========== Theme Editor Frame ==========

    @Override
    public String getThemeEditorTitle() { return "主题包编辑器"; }

    @Override
    public String getThemesBorderTitle() { return "主题列表"; }

    @Override
    public String getPreviewTabTitle() { return "预览"; }

    @Override
    public String getSelectThemeLabel() { return "选择主题:"; }

    @Override
    public String getSaveButtonText() { return "保存配置"; }

    @Override
    public String getOpenFolderButtonText() { return "打开目录"; }

    @Override
    public String getSaveAsButtonText() { return "另存为..."; }

    @Override
    public String getNewThemeButtonText() { return "新建主题"; }

    @Override
    public String getDeleteThemeButtonText() { return "删除主题"; }

    @Override
    public String getAddKeyButtonText() { return "添加键"; }

    @Override
    public String getRemoveKeyButtonText() { return "移除键"; }

    // ========== Dialog Titles & Messages ==========

    @Override
    public String getSavedMessage() { return "配置保存成功！"; }

    @Override
    public String getSavedTitle() { return "已保存"; }

    @Override
    public String getErrorTitle() { return "错误"; }

    @Override
    public String getInfoTitle() { return "提示"; }

    @Override
    public String getNoThemeSelectedTitle() { return "未选择主题"; }

    @Override
    public String getNoThemeSelectedMessage() { return "请从列表中选择一个主题目录进行打包。"; }

    @Override
    public String getNoTarSelectedTitle() { return "未选择 Tar"; }

    @Override
    public String getNoTarSelectedMessage() { return "请从列表中选择一个 .tar 文件进行解包。"; }

    @Override
    public String getConfirmOverwriteTitle() { return "确认覆盖"; }

    @Override
    public String getConfirmOverwriteMessage() { return "文件已存在。是否覆盖？"; }

    @Override
    public String getUnsavedChangesTitle() { return "未保存的更改"; }

    @Override
    public String getUnsavedChangesMessage() { return "配置有未保存的更改。打包前是否保存？"; }

    @Override
    public String getPackSuccessfulTitle() { return "打包成功"; }

    @Override
    public String getPackSuccessfulMessage() { return "成功打包"; }

    @Override
    public String getPackFailedTitle() { return "打包失败"; }

    @Override
    public String getUnpackSuccessfulTitle() { return "解包成功"; }

    @Override
    public String getUnpackSuccessfulMessage() { return "成功解包到: "; }

    @Override
    public String getUnpackFailedTitle() { return "解包失败"; }

    @Override
    public String getSaveAsDialogTitle() { return "另存主题为"; }

    @Override
    public String getNewThemeDialogTitle() { return "新建主题"; }

    @Override
    public String getNewThemeNamePrompt() { return "输入新主题名称:"; }

    @Override
    public String getDeleteConfirmTitle() { return "删除主题"; }

    @Override
    public String getDeleteConfirmMessage() { return "确定要删除此主题吗？"; }

    @Override
    public String getCannotOpenFolderMessage() { return "无法打开目录: "; }

    @Override
    public String getAboutTitle() { return "关于主题包编辑器"; }

    @Override
    public String getAboutMessage() {
        return "SakuraPI Clock - 主题包编辑器\n\n" +
                "用于编辑、打包和解包\n" +
                "SakuraPI Clock 主题包的 GUI 工具。\n\n" +
                "SakuraPI Clock 项目的一部分。";
    }

    // ========== Status Bar ==========

    @Override
    public String getStatusFoundThemes(int count) { return "找到 " + count + " 个主题"; }

    @Override
    public String getStatusConfigSaved() { return "配置已保存"; }

    @Override
    public String getStatusPacked(String name) { return "已打包: " + name + ".tar"; }

    @Override
    public String getStatusUnpacked(String name) { return "已解包: " + name; }

    @Override
    public String getStatusCreatedTheme(String name) { return "已创建新主题: " + name; }

    @Override
    public String getDynamicBackgroundText() { return "动态背景"; }

    @Override
    public String getStaticBackgroundText() { return "静态背景"; }

    // ========== Preview ==========

    @Override
    public String getThemeDirectoryText() { return "主题目录:"; }

    @Override
    public String getTarFileText() { return "Tar 文件:"; }

    @Override
    public String getCannotPreviewText() { return "无法预览"; }

    @Override
    public String getCannotDecodeImageText() { return "无法解码图片"; }

    // ========== Language Info ==========

    @Override
    public String getLanguageName() { return NAME; }

    @Override
    public String getLanguageCode() { return CODE; }
}