package org.sophia.slate_work.casting.contuinations;

/**
 *  For `FrameGetItems`, it has to run over item, but ends to early when using a `list#isEmpty` due to needing to run one more time.
 *  So this horrid thing has to be made, combing the `isFirst` boolean, we can check if the current eval is FIRST, RUNNING, or LAST.
 *  <ul>
 *      <li>FIRST, says if this is the first run, and does not check the stack</li>
 *      <li>RUNNING, says if this is in between the first and last run, and checks the stack</li>
 *      <li>LAST, says if this is the final run, if so queue one more</li>
 *  <ul/>
 */
public enum JankyMaybe {
    FIRST,
    RUNNING,
    PENULTIMATE,
    LAST
}
