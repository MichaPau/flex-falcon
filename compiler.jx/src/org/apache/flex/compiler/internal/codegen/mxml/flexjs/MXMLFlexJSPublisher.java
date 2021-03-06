package org.apache.flex.compiler.internal.codegen.mxml.flexjs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.flex.compiler.codegen.js.IJSPublisher;
import org.apache.flex.compiler.config.Configuration;
import org.apache.flex.compiler.internal.codegen.js.JSSharedData;
import org.apache.flex.compiler.internal.codegen.js.goog.JSGoogPublisher;
import org.apache.flex.compiler.internal.driver.js.goog.JSGoogConfiguration;
import org.apache.flex.compiler.internal.graph.GoogDepsWriter;
import org.apache.flex.compiler.internal.projects.FlexJSProject;
import org.apache.flex.compiler.utils.JSClosureCompilerUtil;

//import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
//import com.google.javascript.jscomp.deps.DepsGenerator;
//import com.google.javascript.jscomp.deps.DepsGenerator.InclusionStrategy;

public class MXMLFlexJSPublisher extends JSGoogPublisher implements
        IJSPublisher
{

    public static final String FLEXJS_OUTPUT_DIR_NAME = "bin";
    public static final String FLEXJS_INTERMEDIATE_DIR_NAME = "js-debug";
    public static final String FLEXJS_RELEASE_DIR_NAME = "js-release";

    public MXMLFlexJSPublisher(Configuration config, FlexJSProject project)
    {
        super(config);

        this.isMarmotinniRun = ((JSGoogConfiguration) configuration)
                .getMarmotinni() != null;
        this.project = project;
    }

    private FlexJSProject project;

    private boolean isMarmotinniRun;

    @Override
    public File getOutputFolder()
    {
        // (erikdebruin) If there is a -marmotinni switch, we want
        //               the output redirected to the directory it specifies.
        if (isMarmotinniRun)
        {
            outputParentFolder = new File(
                    ((JSGoogConfiguration) configuration).getMarmotinni());
        }
        else
        {
            outputParentFolder = new File(
                    configuration.getTargetFileDirectory()).getParentFile();
        }

        outputParentFolder = new File(outputParentFolder,
                FLEXJS_OUTPUT_DIR_NAME);

        outputFolder = new File(outputParentFolder, File.separator
                + FLEXJS_INTERMEDIATE_DIR_NAME);

        // (erikdebruin) Marmotinni handles file management, so we 
        //               bypass the setup.
        if (!isMarmotinniRun)
            setupOutputFolder();

        return outputFolder;
    }

    @Override
    public void publish() throws IOException
    {
        final String intermediateDirPath = outputFolder.getPath();
        final File intermediateDir = new File(intermediateDirPath);
        File srcDir = new File(configuration.getTargetFile());
        srcDir = srcDir.getParentFile();

        final String projectName = FilenameUtils.getBaseName(configuration
                .getTargetFile());
        final String outputFileName = projectName
                + "." + JSSharedData.OUTPUT_EXTENSION;

        File releaseDir = new File(outputParentFolder, FLEXJS_RELEASE_DIR_NAME);
        final String releaseDirPath = releaseDir.getPath();

        if (!isMarmotinniRun)
        {
            if (releaseDir.exists())
                org.apache.commons.io.FileUtils.deleteQuietly(releaseDir);

            releaseDir.mkdirs();
        }

        final String closureLibDirPath = ((JSGoogConfiguration) configuration)
                .getClosureLib();
        final String closureGoogSrcLibDirPath = closureLibDirPath
                + "/closure/goog/";
        final String closureGoogTgtLibDirPath = intermediateDirPath
                + "/library/closure/goog";
        /* AJH not needed by GoogDepsWriter
        final String closureGoogTgtLibDirRelPath = "./library/closure/goog";
        final String closureTPSrcLibDirPath = closureLibDirPath
                + "/third_party/closure/goog/";
        final String closureTPTgtLibDirPath = intermediateDirPath
                + "/library/third_party/closure/goog";
        final List<String> sdkJSLibSrcDirPaths = ((JSGoogConfiguration) configuration)
                .getSDKJSLib();
        final String sdkJSLibTgtDirPath = intermediateDirPath;
        */
        final String depsSrcFilePath = intermediateDirPath
                + "/library/closure/goog/deps.js";
        final String depsTgtFilePath = intermediateDirPath + "/deps.js";
        final String projectIntermediateJSFilePath = intermediateDirPath
                + File.separator + outputFileName;
        final String projectReleaseJSFilePath = releaseDirPath
                + File.separator + outputFileName;

        // just copy base.js. All other goog files should get copied as the DepsWriter chases down goog.requires
        FileUtils.copyFile(new File(closureGoogSrcLibDirPath + File.separator + "base.js"), 
                new File(closureGoogTgtLibDirPath + File.separator + "base.js"));
        GoogDepsWriter gdw = new GoogDepsWriter(intermediateDir, projectName, (JSGoogConfiguration) configuration);
        try
        {
            String depsFileData = gdw.generateDeps();
            writeFile(depsTgtFilePath, depsFileData, false);        
        }
        catch (InterruptedException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        appendExportSymbol(projectIntermediateJSFilePath, projectName);

        if (!isMarmotinniRun)
        {
            //for (String sdkJSLibSrcDirPath : sdkJSLibSrcDirPaths)
            //    copyFile(sdkJSLibSrcDirPath, sdkJSLibTgtDirPath);
        }
        boolean isWindows = System.getProperty("os.name").indexOf("Mac") == -1;

        List<SourceFile> inputs = new ArrayList<SourceFile>();
        Collection<File> files = org.apache.commons.io.FileUtils.listFiles(
                new File(intermediateDirPath),
                new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            if (isWindows)
            {
                // TODO (erikdebruin) maybe fix the 'manual' relative path prefix?
                String filePath = "../../../"
                        + new File(intermediateDirPath).toURI()
                                .relativize(file.toURI()).getPath();

                inputs.add(SourceFile.fromCode(filePath, filePath,
                        readCode(file)));
            }
            else
            {
                inputs.add(SourceFile.fromFile(file));
            }
        }

        if (!isMarmotinniRun)
        {
            //copyFile(closureGoogSrcLibDirPath, closureGoogTgtLibDirPath);
            //copyFile(closureTPSrcLibDirPath, closureTPTgtLibDirPath);
        }

        IOFileFilter pngSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".png"));
        IOFileFilter gifSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".gif"));
        IOFileFilter jpgSuffixFilter = FileFilterUtils.and(FileFileFilter.FILE,
                FileFilterUtils.suffixFileFilter(".jpg"));
        IOFileFilter assetFiles = FileFilterUtils.or(pngSuffixFilter,
                jpgSuffixFilter, gifSuffixFilter);

        FileUtils.copyDirectory(srcDir, intermediateDir, assetFiles);
        FileUtils.copyDirectory(srcDir, releaseDir, assetFiles);

        File srcDeps = new File(depsSrcFilePath);

        final List<SourceFile> deps = new ArrayList<SourceFile>();
        deps.add(SourceFile.fromFile(srcDeps));

//        ErrorManager errorManager = new JSGoogErrorManager();
//        DepsGenerator depsGenerator = new DepsGenerator(deps, inputs,
//                InclusionStrategy.ALWAYS,
//                (isWindows) ? closureGoogTgtLibDirRelPath
//                        : closureGoogTgtLibDirPath, errorManager);
//        writeFile(depsTgtFilePath, depsGenerator.computeDependencyCalls(),
//                false);

        writeHTML("intermediate", projectName, intermediateDirPath);
        writeHTML("release", projectName, releaseDirPath);
        writeCSS(projectName, intermediateDirPath);
        writeCSS(projectName, releaseDirPath);

        ArrayList<String> optionList = new ArrayList<String>();

        files = org.apache.commons.io.FileUtils.listFiles(new File(
                intermediateDirPath), new RegexFileFilter("^.*(\\.js)"),
                DirectoryFileFilter.DIRECTORY);
        for (File file : files)
        {
            optionList.add("--js=" + file.getCanonicalPath());
        }

        optionList.add("--closure_entry_point=" + projectName);
        optionList.add("--only_closure_dependencies");
        optionList.add("--compilation_level=ADVANCED_OPTIMIZATIONS");
        optionList.add("--js_output_file=" + projectReleaseJSFilePath);
        optionList.add("--output_manifest="
                + releaseDirPath + File.separator + "manifest.txt");
        optionList.add("--create_source_map="
                + projectReleaseJSFilePath + ".map");
        optionList.add("--source_map_format=" + SourceMap.Format.V3);

        String[] options = (String[]) optionList.toArray(new String[0]);

        JSClosureCompilerUtil.run(options);

        appendSourceMapLocation(projectReleaseJSFilePath, projectName);

        if (!isMarmotinniRun)
        {
            org.apache.commons.io.FileUtils.deleteQuietly(srcDeps);
            org.apache.commons.io.FileUtils.moveFile(new File(depsTgtFilePath),
                    srcDeps);
        }

        System.out.println("The project '"
                + projectName
                + "' has been successfully compiled and optimized.");
    }

    private void appendExportSymbol(String path, String projectName)
            throws IOException
    {
        StringBuilder appendString = new StringBuilder();
        appendString
                .append("\n\n// Ensures the symbol will be visible after compiler renaming.\n");
        appendString.append("goog.exportSymbol('");
        appendString.append(projectName);
        appendString.append("', ");
        appendString.append(projectName);
        appendString.append(");\n");
        writeFile(path, appendString.toString(), true);
    }

    protected String readCode(File file)
    {
        String code = "";
        try
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(file), "UTF8"));

            String line = in.readLine();

            while (line != null)
            {
                code += line + "\n";
                line = in.readLine();
            }
            code = code.substring(0, code.length() - 1);

            in.close();
        }
        catch (Exception e)
        {
            // nothing to see, move along...
        }

        return code;
    }

    private void writeHTML(String type, String projectName, String dirPath)
            throws IOException
    {
        StringBuilder htmlFile = new StringBuilder();
        htmlFile.append("<!DOCTYPE html>\n");
        htmlFile.append("<html>\n");
        htmlFile.append("<head>\n");
        htmlFile.append("\t<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n");
        htmlFile.append("\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        htmlFile.append("\t<link rel=\"stylesheet\" type=\"text/css\" href=\""
                + projectName + ".css\">\n");

        if (type == "intermediate")
        {
            htmlFile.append("\t<script type=\"text/javascript\" src=\"./library/closure/goog/base.js\"></script>\n");
            htmlFile.append("\t<script type=\"text/javascript\">\n");
            htmlFile.append("\t\tgoog.require(\"");
            htmlFile.append(projectName);
            htmlFile.append("\");\n");
            htmlFile.append("\t</script>\n");
        }
        else
        {
            htmlFile.append("\t<script type=\"text/javascript\" src=\"./");
            htmlFile.append(projectName);
            htmlFile.append(".js\"></script>\n");
        }

        htmlFile.append("</head>\n");
        htmlFile.append("<body>\n");
        htmlFile.append("\t<script type=\"text/javascript\">\n");

        // TODO (erikdebruin) the utility methods should have their own place...
        // is()
        htmlFile.append("\t\tfunction is(object, type) {\n");
        htmlFile.append("\t\t\treturn true;\n");
        htmlFile.append("\t\t};\n");
        htmlFile.append("\t\t\n");
        // int()
        htmlFile.append("\t\tfunction int(value) {\n");
        htmlFile.append("\t\t\treturn value >> 0;\n");
        htmlFile.append("\t\t};\n");
        htmlFile.append("\t\t\n");
        // trace()
        htmlFile.append("\t\tfunction trace(value) {\n");
        htmlFile.append("\t\t\ttry {\n");
        htmlFile.append("\t\t\t\tif (console && console.log) {\n");
        htmlFile.append("\t\t\t\t\tconsole.log(value);\n");
        htmlFile.append("\t\t\t\t}\n");
        htmlFile.append("\t\t\t} catch (e) {\n");
        htmlFile.append("\t\t\t\t// ignore; at least we tried ;-)\n");
        htmlFile.append("\t\t\t}\n");
        htmlFile.append("\t\t};\n");
        htmlFile.append("\t\t\n");
        // uint()
        htmlFile.append("\t\tfunction uint(value) {\n");
        htmlFile.append("\t\t\treturn value >>> 0;\n");
        htmlFile.append("\t\t};\n");
        htmlFile.append("\t\t\n");

        htmlFile.append("\t\tnew ");
        htmlFile.append(projectName);
        htmlFile.append("()");
        htmlFile.append(".start();\n");
        htmlFile.append("\t</script>\n");
        htmlFile.append("</body>\n");
        htmlFile.append("</html>");

        writeFile(dirPath + File.separator + "index.html", htmlFile.toString(),
                false);
    }

    private void writeCSS(String projectName, String dirPath)
            throws IOException
    {
        StringBuilder cssFile = new StringBuilder();
        cssFile.append(project.cssDocument);

        writeFile(dirPath + File.separator + projectName + ".css",
                cssFile.toString(), false);
    }
}
