package data.graph;

/**
 * All vertex labels
 */
public enum Label {
    /**
     * Assigned to vertices representing pins
     */
    PIN,
    /**
     * Assigned to vertices hierarchical components
     */
    COMPONENT,
    /**
     * Assigned to vertices representing links to vertices to hierarchical components.
     */
    PORT,
    /**
     * Assigned to vertices representing the core of a MUX component.
     */
    MUX,
    /**
     * Assigned to vertices representing the input of a component.
     */
    IN,
    /**
     * Assigned to vertices representing the selector of a MUX.
     */
    SELECT,
    /**
     * Assigned to vertices representing the output of a component.
     */
    OUT,
    /**
     * Assigned to vertices representing the core of a LUT component.
     */
    LUT,
    /**
     * Assigned to vertices representing the synchronous reset operation of a register.
     */
    SYNC_RESET,
    /**
     * Assigned to vertices representing the asynchronous reset operation of a register.
     */
    ASYNC_RESET,
    /**
     * Assigned to vertices representing the synchronous set operation of a register.
     */
    SYNC_SET,
    /**
     * Assigned to vertices representing the distinct settings of routing switches.
     */
    OPTION,
    /**
     * Assigned to vertices representing the outer connectors of routing switches.
     */
    SWITCH,
    /**
     * Assigned to vertices representing the the direction towards where the current flows in a routing switch.
     */
    FLOW_TO,
    /**
     * Assigned to vertices representing the the direction from where the current flows in a routing switch.
     */
    FLOW_FROM,
    /**
     * Extra label used for testing.
     */
    EXTRA,
    /**
     * Utility label that will be removed before the algorithm is applied.
     */
    REMOVE,
    /**
     * Assigned to vertices representing the asynchronous set operation of a register.
     */
    ASYNC_SET,
    /**
     * Assigned to vertices representing the clock-enable operation of a register.
     */
    CLOCK_ENABLE,
    /**
     * A Hierarchygraph may have two of these vertices: they represent the falling- and rising clock edge.
     */
    CLOCK_FRAME,
    /**
     * Assigned to vertices representing the core of a Register component.
     */
    REGISTER;
}
