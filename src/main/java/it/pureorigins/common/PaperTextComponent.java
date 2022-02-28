package it.pureorigins.common;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Arrays;
import java.util.Collection;

public class PaperTextComponent implements ArgumentType<Component> {
  private static final Collection<String> EXAMPLES = Arrays.asList("\"hello world\"", "\"\"", "\"{\"text\":\"hello world\"}", "[\"\"]");
  public static final DynamicCommandExceptionType ERROR_INVALID_JSON = new DynamicCommandExceptionType((text) -> new TranslatableComponent("argument.component.invalid", text));
  
  private PaperTextComponent() {
  }
  
  public static Component getPaperText(CommandContext<CommandSourceStack> context, String name) {
    return context.getArgument(name, Component.class);
  }
  
  public static PaperTextComponent paperText() {
    return new PaperTextComponent();
  }
  
  public Component parse(StringReader reader) throws CommandSyntaxException {
    try {
      return GsonComponentSerializer.gson().deserialize(reader.getString());
    } catch (Exception e) {
      String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw ERROR_INVALID_JSON.createWithContext(reader, message);
    }
  }
  
  public Collection<String> getExamples() {
    return EXAMPLES;
  }
}
