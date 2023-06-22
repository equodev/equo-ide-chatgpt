package dev.equo.ide.chatgpt;

import dev.equo.ide.IdeHook;
import dev.equo.ide.IdeHookInstantiated;

public class ChatGptIdeHook implements IdeHook {
    @Override
    public IdeHookInstantiated instantiate() throws Exception {
        return IdeHook.usingReflection("dev.equo.ide.chatgpt.ChatGptIdeHookImpl", this);
    }
}
