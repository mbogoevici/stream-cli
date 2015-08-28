/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.cli.command;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import joptsimple.OptionSet;

import org.springframework.boot.cli.command.install.GroovyDependencyResolver;
import org.springframework.boot.cli.command.options.CompilerOptionHandler;
import org.springframework.boot.cli.command.options.OptionSetGroovyCompilerConfiguration;
import org.springframework.boot.cli.command.run.SpringApplicationRunner;
import org.springframework.boot.cli.command.run.SpringApplicationRunnerConfiguration;
import org.springframework.boot.cli.command.status.ExitStatus;
import org.springframework.boot.cli.compiler.GroovyCompilerConfiguration;
import org.springframework.boot.cli.compiler.GroovyCompilerScope;
import org.springframework.boot.cli.compiler.RepositoryConfigurationFactory;
import org.springframework.boot.cli.compiler.grape.RepositoryConfiguration;
import org.springframework.boot.loader.archive.JarFileArchive;

/**
 * @author Marius Bogoevici
 */
public class RunModuleCommandHandler extends CompilerOptionHandler {

	@Override
	protected ExitStatus run(OptionSet options) throws Exception {
		String moduleReference = (String) options.nonOptionArguments().get(0);
		System.out.println("Module run: " + moduleReference);
		List<RepositoryConfiguration> repositoryConfiguration = RepositoryConfigurationFactory
				.createDefaultRepositoryConfiguration();
		GroovyCompilerConfiguration groovyCompilerConfiguration = new OptionSetGroovyCompilerConfiguration(options, this, repositoryConfiguration);
		GroovyDependencyResolver groovyDependencyResolver = new GroovyDependencyResolver(groovyCompilerConfiguration);
		List<File> resolvedFiles = groovyDependencyResolver.resolve(Arrays.asList(moduleReference + ":exec"));
		for (File resolvedFile : resolvedFiles) {
			System.out.println(resolvedFile);
		}
		JarFileArchive archive = new JarFileArchive(resolvedFiles.get(0));
		File generatedScript = File.createTempFile(UUID.randomUUID().toString(), ".groovy");
		generatedScript.deleteOnExit();
		PrintWriter printWriter = new PrintWriter(new FileWriter(generatedScript));
		printWriter.println("@Grab('" + moduleReference + "')");
//		printWriter.println("@Import(" + archive.getMainClass() + ")");
//		printWriter.println("class Module {}");

//		printWriter.println("@Import(" + archive.getMainClass() + ")");
		printWriter.println("import org.springframework.boot.SpringApplication;");
		printWriter.println();
		printWriter.println("SpringApplication.run(" + archive.getMainClass() + ")");
		printWriter.flush();
		printWriter.close();
		SpringApplicationRunnerConfiguration springApplicationRunnerConfiguration = new SpringApplicationRunnerConfigurationAdapter(options, this, repositoryConfiguration);
		System.out.println(generatedScript.getAbsolutePath());
		SpringApplicationRunner springApplicationRunner = new SpringApplicationRunner(springApplicationRunnerConfiguration,
				new String[]{generatedScript.getAbsolutePath()});
		springApplicationRunner.compileAndRun();
		return ExitStatus.OK;
	}

	/**
	 * Simple adapter class to present the {@link OptionSet} as a
	 * {@link SpringApplicationRunnerConfiguration}.
	 */
	private class SpringApplicationRunnerConfigurationAdapter extends
			OptionSetGroovyCompilerConfiguration implements
			SpringApplicationRunnerConfiguration {

		public SpringApplicationRunnerConfigurationAdapter(OptionSet options,
																											 CompilerOptionHandler optionHandler,
																											 List<RepositoryConfiguration> repositoryConfiguration) {
			super(options, optionHandler, repositoryConfiguration);
		}

		@Override
		public GroovyCompilerScope getScope() {
			return GroovyCompilerScope.DEFAULT;
		}

		@Override
		public boolean isWatchForFileChanges() {
			return false;
		}

		@Override
		public Level getLogLevel() {
			return Level.INFO;
		}
	}
}

