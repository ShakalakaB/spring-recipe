package aldora.spring.recipe.services;

import aldora.spring.recipe.commands.IngredientCommand;
import aldora.spring.recipe.commands.UnitOfMeasureCommand;
import aldora.spring.recipe.converters.IngredientCommandToIngredient;
import aldora.spring.recipe.converters.IngredientToIngredientCommand;
import aldora.spring.recipe.converters.UnitOfMeasureCommandToUnitOfMeasure;
import aldora.spring.recipe.converters.UnitOfMeasureToUnitOfMeasureCommand;
import aldora.spring.recipe.model.Ingredient;
import aldora.spring.recipe.model.Recipe;
import aldora.spring.recipe.model.UnitOfMeasure;
import aldora.spring.recipe.repositories.RecipeRepository;
import aldora.spring.recipe.repositories.UnitOfMeasureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;

import javax.persistence.criteria.CriteriaBuilder;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IngredientServiceImplTest {
    public static final long ID = 1L;
    public static final String UNIT_DESC = "unit desc";
    public static final BigDecimal AMOUNT = BigDecimal.valueOf(2);
    public static final String INGREDIENT_DESC = "ingredient desc";
    @Mock
    RecipeRepository recipeRepository;

    @Mock
    UnitOfMeasureRepository unitOfMeasureRepository;

    IngredientServiceImpl ingredientService;

    Ingredient existingIngredient;

    UnitOfMeasure unitOfMeasure;

    Recipe recipe;

    IngredientCommandToIngredient ingredientCommandToIngredient;

    UnitOfMeasureCommandToUnitOfMeasure unitOfMeasureCommandToUnitOfMeasure;

    IngredientToIngredientCommand ingredientToIngredientCommand;

    UnitOfMeasureToUnitOfMeasureCommand unitOfMeasureToUnitOfMeasureCommand;

    @BeforeEach
    void setUp() {
        unitOfMeasure = new UnitOfMeasure();
        unitOfMeasure.setId(ID);
        unitOfMeasure.setDescription(UNIT_DESC);

        existingIngredient = new Ingredient();
        existingIngredient.setId(ID);
        existingIngredient.setAmount(AMOUNT);
        existingIngredient.setDescription(INGREDIENT_DESC);
        existingIngredient.setUnitOfMeasure(unitOfMeasure);

        recipe = new Recipe();
        recipe.setId(ID);
        recipe.addIngredient(existingIngredient);

        unitOfMeasureCommandToUnitOfMeasure = new UnitOfMeasureCommandToUnitOfMeasure();
        ingredientCommandToIngredient = new IngredientCommandToIngredient(unitOfMeasureCommandToUnitOfMeasure);
        unitOfMeasureToUnitOfMeasureCommand = new UnitOfMeasureToUnitOfMeasureCommand();
        ingredientToIngredientCommand = new IngredientToIngredientCommand(unitOfMeasureToUnitOfMeasureCommand);

        MockitoAnnotations.openMocks(this);
        ingredientCommandToIngredient = new IngredientCommandToIngredient(new UnitOfMeasureCommandToUnitOfMeasure());
        ingredientService = new IngredientServiceImpl(recipeRepository, ingredientToIngredientCommand,
                unitOfMeasureRepository, ingredientCommandToIngredient);
    }

    @Test
    void findByRecipeIdAndIngredientId() {
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setId(1L);

        Ingredient ingredient2 = new Ingredient();
        ingredient2.setId(2L);

        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.getIngredients().add(ingredient1);
        recipe.getIngredients().add(ingredient2);

        when(recipeRepository.findById(anyLong())).thenReturn(Optional.of(recipe));

        IngredientCommand ingredientCommand = ingredientService.findByRecipeIdAndIngredientId(1L, 1L);

        assertEquals(1L, ingredientCommand.getId());
        verify(recipeRepository, times(1)).findById(anyLong());
    }

    @Test
    void saveIngredientCommand() {
        IngredientCommand unsavedIngredientCommand = getIngredientCommand();
        IngredientCommand ingredientCommand = getIngredientCommand();
        ingredientCommand.setId(2L);

        when(recipeRepository.findById(anyLong())).thenReturn(Optional.of(new Recipe()));

        recipe.addIngredient(ingredientCommandToIngredient.convert(ingredientCommand));

        when(recipeRepository.save(any())).thenReturn(recipe);

        IngredientCommand savedIngredientCommand = ingredientService.saveOrUpdateIngredientCommand(ingredientCommand);

        assertEquals(unsavedIngredientCommand.getAmount(),savedIngredientCommand.getAmount());
        assertEquals(unsavedIngredientCommand.getDescription(), savedIngredientCommand.getDescription());
        assertEquals(unsavedIngredientCommand.getUnitOfMeasure().getId(), savedIngredientCommand.getUnitOfMeasure().getId());
    }

    @Test
    void updateIngredientCommand() {
        IngredientCommand ingredientCommand = new IngredientCommand();
        ingredientCommand.setId(ID);
        ingredientCommand.setRecipeId(recipe.getId());
        ingredientCommand.setAmount(BigDecimal.valueOf(99));
        ingredientCommand.setDescription("new desc");
        ingredientCommand.setUnitOfMeasure(unitOfMeasureToUnitOfMeasureCommand.convert(unitOfMeasure));

        Recipe newRecipe = new Recipe();
        newRecipe.setId(ID);
        newRecipe.addIngredient(ingredientCommandToIngredient.convert(ingredientCommand));

        when(recipeRepository.findById(anyLong())).thenReturn(Optional.of(recipe));

        when(unitOfMeasureRepository.findById(anyLong())).thenReturn(Optional.of(unitOfMeasure));
        when(recipeRepository.save(any())).thenReturn(newRecipe);

        IngredientCommand savedIngredientCommand = ingredientService.saveOrUpdateIngredientCommand(ingredientCommand);

        assertEquals(savedIngredientCommand.getId(), ingredientCommand.getId());
    }

    private IngredientCommand getIngredientCommand() {
        UnitOfMeasureCommand unitOfMeasureCommand = new UnitOfMeasureCommand();
        unitOfMeasureCommand.setId(2L);

        IngredientCommand ingredientCommand = new IngredientCommand();
        ingredientCommand.setUnitOfMeasure(unitOfMeasureCommand);
        ingredientCommand.setAmount(BigDecimal.valueOf(3));
        ingredientCommand.setDescription("ingredientCommand desc");
        ingredientCommand.setRecipeId(recipe.getId());

        return ingredientCommand;
    }
}