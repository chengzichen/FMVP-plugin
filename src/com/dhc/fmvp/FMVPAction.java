package com.dhc.fmvp;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.*;

public class FMVPAction extends AnAction {
    Project project;
    VirtualFile selectGroup;

    @Override
    public void update(AnActionEvent event) {
        super.update(event);
        Presentation presentation = event.getPresentation();
        if (isRightPackage(event)) {
            presentation.setEnabledAndVisible(true);
        } else {
            presentation.setEnabledAndVisible(false);
        }
    }


    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getProject();
        String className = Messages.showInputDialog(project, "请输入类名称", "新建FMVP模板", Messages.getQuestionIcon());
        if (isEmpty(className == null, className.equals(""))) {
            Messages.showErrorDialog(
                    "You have to type in something.",
                    "content is empty");
            return;
        }
        createClassMvp(className);
        project.getBaseDir().refresh(false, true);
    }

    private boolean isEmpty(boolean b, boolean equals) {
        return b || equals;
    }


    private boolean isRightPackage(AnActionEvent actionEvent) {
        selectGroup = DataKeys.VIRTUAL_FILE.getData(actionEvent.getDataContext());
        String packageName = selectGroup.getPath();
        System.out.println("packageName"+ packageName);
        if (packageName==null||packageName.equals(""))
            return false;
        String[] subPackages = packageName.split("\\/");
        for (String subPackage : subPackages) {
            if (subPackage.endsWith("java")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据模块生成代码
     *
     * @param className
     */
    private void createClassMvp(String className) {
        boolean isFragment = isEmpty(className.endsWith("Fragment"), className.endsWith("fragment"));
        boolean isActivity = isEmpty(className.endsWith("Activity"), className.endsWith("activity"));
        if (className.endsWith("Fragment") || className.endsWith("fragment") || className.endsWith("Activity") || className.endsWith("activity")) {
            className = className.substring(0, className.length() - 8);
        }
        String contractPath = selectGroup.getPath() + "/presenter/contract";
        String presenterPath = selectGroup.getPath() + "/presenter";
        String uiPath = selectGroup.getPath() + "/ui";
        String modlePath = selectGroup.getPath() + "/modle";
        className = className.substring(0, 1).toUpperCase() + className.substring(1);
        String contract = readFile("Contract.txt")
                .replace("&package&", getPackageName(contractPath))
                .replace("&Contract&", "I" + className + "Contract");
        String presenter = readFile("Presenter.txt")
                .replace("&package&", getPackageName(presenterPath))
                .replace("&Module&", className)
                .replace("&Contract&", "I" + className + "Contract")
                .replace("&ContractPackageName&", getPackageName(contractPath))
                .replace("&DataServicePackageName&", getPackageName(modlePath))
                .replace("&Presenter&", className + "Presenter");
        String dataService = readFile("DataService.txt")
                .replace("&package&", getPackageName(modlePath))
                .replace("&Module&", className)
                .replace("&ContractPackageName&", getPackageName(contractPath))
                .replace("&Contract&", "I" + className + "Contract");

        if (isFragment) {
            String fragment = readFile("Fragment.txt")
                    .replace("&package&", getPackageName(uiPath))
                    .replace("&Fragment&", className + "Fragment")
                    .replace("&ContractPackageName&", getPackageName(contractPath))
                    .replace("&Contract&", "I" + className + "Contract")
                    .replace("&Presenter&", className + "Presenter");
            writetoFile(fragment, uiPath, className + "Fragment.java");
        } else if (isActivity) {
            String activity = readFile("Activity.txt")
                    .replace("&package&", getPackageName(uiPath))
                    .replace("&Activity&", className + "Activity")
                    .replace("&ContractPackageName&", getPackageName(contractPath))
                    .replace("&Contract&", "I" + className + "Contract")
                    .replace("&Presenter&", className + "Presenter");
            writetoFile(activity, uiPath, className + "Activity.java");
        }
        writetoFile(contract, contractPath, "I" + className + "Contract.java");
        writetoFile(presenter, presenterPath, className + "Presenter.java");
        writetoFile(dataService, modlePath, className + "RemoteDataService.java");
    }

    private String getPackageName(String path) {
        return path.substring(path.indexOf("java") + 5, path.length()).replace("/", ".");
    }


    private String readFile(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("code/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
        }
        return content;
    }

    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

}
