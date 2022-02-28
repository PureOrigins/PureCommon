package it.pureorigins.common;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Collection;

public class SpigotTextComponent implements ArgumentType<BaseComponent[]> {
  private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
  public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType((text) -> new TranslatableComponent("argument.component.invalid", text));
  
  private SpigotTextComponent() {
  }
  
  public static BaseComponent[] getSpigotText(CommandContext<CommandSourceStack> context, String name) {
    return context.getArgument(name, BaseComponent[].class);
  }
  
  public static SpigotTextComponent spigotText() {
    return new SpigotTextComponent();
  }
  
  public BaseComponent[] parse(StringReader reader) throws CommandSyntaxException {
    try {
      BaseComponent[] component = ComponentSerializer.parse(reader.getString());
      if (component == null) {
        throw ERROR_INVALID_JSON.createWithContext(reader, "empty");
      } else {
        return component;
      }
    } catch (Exception e) {
      String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw ERROR_INVALID_JSON.createWithContext(reader, message);
    }
  }
  
  public Collection<String> getExamples() {
    return EXAMPLES;
  }
}
