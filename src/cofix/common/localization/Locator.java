/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.agent.AgentCreator;
import com.gzoltar.core.agent.Launcher;
import com.gzoltar.core.components.Component;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.exec.parameters.ClassParameters;
import com.gzoltar.core.exec.parameters.TestParameters;
import com.gzoltar.core.instr.message.Response;
import com.gzoltar.core.instr.testing.TestResult;
import com.gzoltar.core.spectra.Spectra;

import cofix.common.localization.alg.Algorithm;

/**
 * @author Jiajun
 * @date Jun 21, 2017
 */
public class Locator extends GZoltar{
	private File agent = null;
	private String workingDirectory;
	private ClassParameters classParameters;
	private TestParameters testParameters;
	private ArrayList<String> classpaths;
	private Spectra spectra;
	private Algorithm algorithm;

	public Locator(String wD, Algorithm alg) throws FileNotFoundException, IOException {
		super(wD);
		this.workingDirectory = wD;
		this.classParameters = new ClassParameters();
		this.testParameters = new TestParameters();
		this.classpaths = new ArrayList<>();
		this.algorithm = alg;
		this.agent = AgentCreator.extract(
				new String[] { "com/gzoltar/core/components", "com/gzoltar/core/instr", "com/gzoltar/core/exec",
						"junit", "org/junit", "org/hamcrest", "org/objectweb/asm", "com/google/common" });
	}

	public void run() {
		Response r = launchAgent();

		this.spectra = new Spectra();
		this.spectra.registerResults(r.getTestResults());

		SBFL.sfl(this.spectra, algorithm);
	}

	public static void main(String[] args) throws IOException {
	}

	public List<TestResult> getTestResults() {
		return this.spectra.getTestResults();
	}

	public void addClassToInstrument(String name) {
		this.classParameters.addClassToInstrument(name);
	}

	public void addPackageToInstrument(String name) {
		this.classParameters.addPackageToInstrument(name);
	}

	public void addClassNotToInstrument(String name) {
		this.classParameters.addClassNotToInstrument(name);
	}

	public void addPackageNotToInstrument(String name) {
		this.classParameters.addPackageNotToInstrument(name);
	}

	public void addTestToExecute(String name) {
		this.testParameters.addTestToExecute(name);
	}

	public void addTestPackageToExecute(String name) {
		this.testParameters.addTestPackageToExecute(name);
	}

	public void addTestNotToExecute(String name) {
		this.testParameters.addTestNotToExecute(name);
	}

	public void addTestPackageNotToExecute(String name) {
		this.testParameters.addTestPackageNotToExecute(name);
	}

	public ClassParameters getClassParameters() {
		return this.classParameters;
	}

	public void setClassParameters(ClassParameters classParameters) {
		this.classParameters = classParameters;
	}

	public TestParameters getTestParameters() {
		return this.testParameters;
	}

	public void setTestParameters(TestParameters testParameters) {
		this.testParameters = testParameters;
	}

	public String getWorkingDirectory() {
		return this.workingDirectory;
	}

	public void setWorkingDirectory(String wD) {
		this.workingDirectory = wD;
	}

	public ArrayList<String> getClasspaths() {
		return this.classpaths;
	}

	public void setClassPaths(ArrayList<String> cPs) {
		this.classpaths.addAll(cPs);
	}

	public Spectra getSpectra() {
		return this.spectra;
	}

	public List<Component> getSuspiciousComponents() {
		return this.spectra.getComponents();
	}

	public List<Statement> getSuspiciousStatements() {
		List<Component> allComponents = this.spectra.getComponents();
		List<Statement> statements = new ArrayList<>();
		for (Component c : allComponents) {
			if (c instanceof Statement)
				statements.add((Statement) c);
		}
		return statements;
	}

	private Response launchAgent() {
		if (this.agent != null)
			return Launcher.launch(this, this.agent);
		return null;
	}
	
}
