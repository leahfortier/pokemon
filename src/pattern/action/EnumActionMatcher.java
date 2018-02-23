package pattern.action;

import gui.view.ViewMode;
import map.triggers.BadgeTrigger;
import map.triggers.ChangeViewTrigger;
import map.triggers.CommonTrigger;
import map.triggers.MedalCountTrigger;
import map.triggers.SoundTrigger;
import map.triggers.Trigger;
import mapMaker.dialogs.action.ActionType;
import sound.SoundTitle;
import trainer.player.Badge;
import trainer.player.medal.MedalTheme;

public interface EnumActionMatcher<T extends Enum> extends ActionMatcher {
    T getEnumValue();

    class BadgeActionMatcher implements EnumActionMatcher<Badge> {
        private Badge badge;

        public BadgeActionMatcher(Badge badge) {
            this.badge = badge;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.BADGE;
        }

        @Override
        public Trigger createNewTrigger() {
            return new BadgeTrigger(this.badge);
        }

        @Override
        public Badge getEnumValue() {
            return badge;
        }
    }

    class ChangeViewActionMatcher implements EnumActionMatcher<ViewMode> {
        private ViewMode viewMode;

        public ChangeViewActionMatcher(ViewMode viewMode) {
            this.viewMode = viewMode;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.CHANGE_VIEW;
        }

        @Override
        public Trigger createNewTrigger() {
            return new ChangeViewTrigger(this.viewMode);
        }

        @Override
        public ViewMode getEnumValue() {
            return viewMode;
        }
    }

    class SoundActionMatcher implements EnumActionMatcher<SoundTitle> {
        private SoundTitle soundTitle;

        public SoundActionMatcher(SoundTitle soundTitle) {
            this.soundTitle = soundTitle;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.SOUND;
        }

        @Override
        public Trigger createNewTrigger() {
            return new SoundTrigger(soundTitle);
        }

        @Override
        public SoundTitle getEnumValue() {
            return soundTitle;
        }
    }

    class MedalCountActionMatcher implements EnumActionMatcher<MedalTheme> {
        private MedalTheme medalTheme;

        public MedalCountActionMatcher(MedalTheme medalTheme) {
            this.medalTheme = medalTheme;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.MEDAL_COUNT;
        }

        @Override
        public Trigger createNewTrigger() {
            return new MedalCountTrigger(medalTheme);
        }

        @Override
        public MedalTheme getEnumValue() {
            return medalTheme;
        }
    }

    class CommonTriggerActionMatcher implements EnumActionMatcher<CommonTrigger> {
        private CommonTrigger triggerName;

        public CommonTriggerActionMatcher(CommonTrigger triggerName) {
            this.triggerName = triggerName;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.COMMON_TRIGGER;
        }

        @Override
        public Trigger createNewTrigger() {
            return triggerName.getTrigger();
        }

        @Override
        public CommonTrigger getEnumValue() {
            return triggerName;
        }
    }
}
