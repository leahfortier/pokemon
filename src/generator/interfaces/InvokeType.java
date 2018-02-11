package generator.interfaces;

import generator.interfaces.InvokeMethod.AddInvoke;
import generator.interfaces.InvokeMethod.CheckGetInvoke;
import generator.interfaces.InvokeMethod.CheckInvoke;
import generator.interfaces.InvokeMethod.CheckMessageInvoke;
import generator.interfaces.InvokeMethod.ContainsInvoke;
import generator.interfaces.InvokeMethod.GetInvoke;
import generator.interfaces.InvokeMethod.MultiplyInvoke;
import generator.interfaces.InvokeMethod.UpdateInvoke;
import generator.interfaces.InvokeMethod.VoidInvoke;
import main.Global;
import util.StringUtils;

import java.util.Scanner;

enum InvokeType {
    VOID(input -> new VoidInvoke()),
    CONTAINS(input -> new ContainsInvoke()),
    CHECK(CheckInvoke::new),
    CHECK_GET(CheckGetInvoke::new),
    CHECK_MESSAGE(CheckMessageInvoke::new),
    GET(input -> new GetInvoke()),
    UPDATE(input -> new UpdateInvoke()),
    MULTIPLY(input -> new MultiplyInvoke()),
    ADD(input -> new AddInvoke());

    private final GetInvokeMethod getInvokeMethod;

    InvokeType(final GetInvokeMethod getInvokeMethod) {
        this.getInvokeMethod = getInvokeMethod;
    }

    public InvokeMethod getInvokeMethod(final Scanner invokeInput) {
        InvokeMethod invokeMethod = this.getInvokeMethod.getInvokeMethod(invokeInput);
        if (invokeInput.hasNext()) {
            Global.error("Too much input for " + this.getClass().getSimpleName() + ": " + invokeInput);
        }

        return invokeMethod;
    }

    public static InvokeMethod getInvokeMethod(String name, Scanner in) {
        try {
            return InvokeType.valueOf(StringUtils.getNamesiesString(name)).getInvokeMethod(in);
        } catch (IllegalArgumentException exception) {
            Global.error("Invalid invoke type value: " + name);
            return null;
        }
    }

    @FunctionalInterface
    private interface GetInvokeMethod {
        InvokeMethod getInvokeMethod(Scanner invokeInput);
    }
}
