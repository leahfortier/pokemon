package generator;

import battle.effect.generic.EffectInterfaces;
import main.Global;
import util.FileIO;
import util.FileName;
import util.Folder;
import util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

class InterfaceGen {
	private static final String INTERFACE_PATH = Folder.GENERIC_EFFECT + EffectInterfaces.class.getSimpleName() + ".java";
	
	InterfaceGen() {
		gen();
	}
	
	private static void gen() {
		final Scanner in = FileIO.openFile(FileName.INTERFACES);
		final StringBuilder out = StuffGen.startGen(INTERFACE_PATH);
		
		// Go through the entire file
		while (in.hasNext()) {
			
			String line = in.nextLine().trim();
			
			// Ignore comments and white space at beginning of file
			if (line.isEmpty() || line.startsWith("#")) {
				continue;
			}
			
			final String interfaceName = line.replace(":", "");
			final Interface effectInterface = new Interface(in, interfaceName);
			
			out.append(effectInterface.writeInterface());
		}

		out.append("}");
		
		FileIO.overwriteFile(INTERFACE_PATH, out);
	}
	
	private static class Interface {
		private static final String COMMENTS = "Comments";
		private static final String METHOD = "Method";
		private static final String EXTENDS = "Extends";

		private final String interfaceName;
		
		private String headerComments;
		private String extendsInterfaces;
		private List<InterfaceMethod> methods;
		
		Interface(final Scanner in, final String interfaceName) {
			this.interfaceName = interfaceName;
			
			this.headerComments = StringUtils.empty();
			this.extendsInterfaces = StringUtils.empty();
			this.methods = new LinkedList<>();
			
			readInterface(in);
		}
		
		private void readInterface(Scanner in) {
			while (in.hasNextLine()) {
				String line = in.nextLine().trim();
				
				if (line.equals("***")) {
					break;
				}
				
				final String[] split = line.split(":", 2);
				final String fieldKey = split[0].trim();
				
				switch (fieldKey) {
					case COMMENTS:
						// TODO: Right now class comment is restricted to a single line
						this.headerComments = getSingleLineInput(COMMENTS, split);
						break;
					case METHOD:
						final ClassFields fields = StuffGen.readFields(in);
						this.methods.add(new InterfaceMethod(this.interfaceName, fields));
						break;
					case EXTENDS:
						this.extendsInterfaces = getSingleLineInput(EXTENDS, split);
						break;
					default:
						Global.error("Invalid key name " + fieldKey + " for interface " + this.interfaceName);
						break;
				}
			}
		}

		private String getSingleLineInput(String key, String[] split) {
			if (split.length != 2) {
				Global.error(key + " for " + this.interfaceName + " must be on a single line.");
			}

			final String fieldValue = split[1].trim();
			if (fieldValue.isEmpty()) {
				Global.error(key + " for " + this.interfaceName + " is empty.");
			}

			return fieldValue;
		}
		
		String writeInterface() {
			final String superClass = this.extendsInterfaces;
			final String interfaces = null;
			
			final StringBuilder extraFields = new StringBuilder();
			for (InterfaceMethod method : this.methods) {
				extraFields.append(method.writeInterfaceMethod());
			}
			
			final String constructor = null;
			final StringBuilder additional = new StringBuilder();
			for (InterfaceMethod method : this.methods) {
				additional.append(method.writeInvokeMethod());
			}

			return StuffGen.createClass(
					this.headerComments,
					this.interfaceName,
					superClass,
					interfaces,
					extraFields.toString(),
					constructor,
					additional.toString(),
					true);
		}
	}
}
