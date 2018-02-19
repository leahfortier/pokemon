package pattern.action;

import gui.view.ViewMode;
import map.condition.Condition;
import map.triggers.BadgeTrigger;
import map.triggers.ChangeViewTrigger;
import map.triggers.MedalCountTrigger;
import map.triggers.SoundTrigger;
import map.triggers.Trigger;
import mapMaker.dialogs.action.ActionType;
import sound.SoundTitle;
import trainer.player.Badge;
import trainer.player.medal.MedalTheme;

public abstract class EnumActionMatcher<T extends Enum> extends ActionMatcher {
    public abstract T getEnumValue();

    public static class BadgeActionMatcher extends EnumActionMatcher<Badge> {
        private Badge badge;

        public BadgeActionMatcher(Badge badge) {
            this.badge = badge;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.BADGE;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new BadgeTrigger(this.badge, condition);
        }

        @Override
        public Badge getEnumValue() {
            return badge;
        }
    }

    public static class ChangeViewActionMatcher extends EnumActionMatcher<ViewMode> {
        private ViewMode viewMode;

        public ChangeViewActionMatcher(ViewMode viewMode) {
            this.viewMode = viewMode;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.CHANGE_VIEW;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new ChangeViewTrigger(this.viewMode, condition);
        }

        @Override
        public ViewMode getEnumValue() {
            return viewMode;
        }
    }

    public static class SoundActionMatcher extends EnumActionMatcher<SoundTitle> {
        private SoundTitle soundTitle;

        public SoundActionMatcher(SoundTitle soundTitle) {
            this.soundTitle = soundTitle;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.SOUND;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new SoundTrigger(soundTitle, condition);
        }

        @Override
        public SoundTitle getEnumValue() {
            return soundTitle;
        }
    }

    public static class MedalCountActionMatcher extends EnumActionMatcher<MedalTheme> {
        private MedalTheme medalTheme;

        public MedalCountActionMatcher(MedalTheme medalTheme) {
            this.medalTheme = medalTheme;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.MEDAL_COUNT;
        }

        @Override
        protected Trigger getTrigger(String entityName, Condition condition) {
            return new MedalCountTrigger(medalTheme, condition);
        }

        @Override
        public MedalTheme getEnumValue() {
            return medalTheme;
        }
    }
}
