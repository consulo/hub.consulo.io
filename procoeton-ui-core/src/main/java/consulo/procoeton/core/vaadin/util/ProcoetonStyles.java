package consulo.procoeton.core.vaadin.util;

import com.vaadin.flow.component.HasStyle;

import java.util.Objects;

@SuppressWarnings("unchecked")
public final class ProcoetonStyles {
    private ProcoetonStyles() {
    }

    public static <T extends HasStyle> T apply(T target, String... classNames) {
        Objects.requireNonNull(target, "target");
        if (classNames != null) {
            for (String cn : classNames) {
                if (cn != null && !cn.isBlank()) {
                    target.addClassName(cn);
                }
            }
        }
        return target;
    }

  /* =========================
     Display
     ========================= */

    public static final class Display {
        private Display() {
        }

        public static final String NONE = "procoeton-ui-hidden";
        public static final String BLOCK = "procoeton-ui-block";
        public static final String INLINE = "procoeton-ui-inline";
        public static final String INLINE_BLOCK = "procoeton-ui-inline-block";
        public static final String FLEX = "procoeton-ui-flex";
        public static final String INLINE_FLEX = "procoeton-ui-inline-flex";
    }

  /* =========================
     Flex
     ========================= */

    public static final class FlexDirection {
        private FlexDirection() {
        }

        public static final String ROW = "procoeton-ui-row";
        public static final String ROW_REVERSE = "procoeton-ui-row-reverse";
        public static final String COLUMN = "procoeton-ui-col";
        public static final String COLUMN_REVERSE = "procoeton-ui-col-reverse";
    }

    public static final class AlignItems {
        private AlignItems() {
        }

        public static final String START = "procoeton-ui-items-start";
        public static final String CENTER = "procoeton-ui-items-center";
        public static final String END = "procoeton-ui-items-end";
        public static final String STRETCH = "procoeton-ui-items-stretch";
    }

    public static final class JustifyContent {
        private JustifyContent() {
        }

        public static final String START = "procoeton-ui-justify-start";
        public static final String CENTER = "procoeton-ui-justify-center";
        public static final String END = "procoeton-ui-justify-end";
        public static final String BETWEEN = "procoeton-ui-justify-between";
        public static final String AROUND = "procoeton-ui-justify-around";
        public static final String EVENLY = "procoeton-ui-justify-evenly";
    }

  /* =========================
     Gap
     ========================= */

    public static final class Gap {
        private Gap() {
        }

        public static final String NONE = "procoeton-ui-gap-0";
        public static final String XXSMALL = "procoeton-ui-gap-xxs";
        public static final String XSMALL = "procoeton-ui-gap-xs";
        public static final String SMALL = "procoeton-ui-gap-s";
        public static final String MEDIUM = "procoeton-ui-gap-m";
        public static final String LARGE = "procoeton-ui-gap-l";
        public static final String XLARGE = "procoeton-ui-gap-xl";
    }

  /* =========================
     Width / Height
     ========================= */

    public static final class Width {
        private Width() {
        }

        public static final String AUTO = "procoeton-ui-w-auto";
        public static final String FULL = "procoeton-ui-w-full";
    }

    public static final class Height {
        private Height() {
        }

        public static final String AUTO = "procoeton-ui-h-auto";
        public static final String FULL = "procoeton-ui-h-full";

        public static final String XXSMALL = "procoeton-ui-h-xxs";
        public static final String XSMALL = "procoeton-ui-h-xs";
        public static final String SMALL = "procoeton-ui-h-s";
        public static final String MEDIUM = "procoeton-ui-h-m";
        public static final String LARGE = "procoeton-ui-h-l";
        public static final String XLARGE = "procoeton-ui-h-xl";
    }

  /* =========================
     Padding
     ========================= */

    public static final class Padding {
        private Padding() {
        }

        public static final String NONE = "procoeton-ui-p-0";
        public static final String XXSMALL = "procoeton-ui-p-xxs";
        public static final String XSMALL = "procoeton-ui-p-xs";
        public static final String SMALL = "procoeton-ui-p-s";
        public static final String MEDIUM = "procoeton-ui-p-m";
        public static final String LARGE = "procoeton-ui-p-l";
        public static final String XLARGE = "procoeton-ui-p-xl";

        public static final class Horizontal {
            private Horizontal() {
            }

            public static final String NONE = "procoeton-ui-px-0";
            public static final String XXSMALL = "procoeton-ui-px-xxs";
            public static final String XSMALL = "procoeton-ui-px-xs";
            public static final String SMALL = "procoeton-ui-px-s";
            public static final String MEDIUM = "procoeton-ui-px-m";
            public static final String LARGE = "procoeton-ui-px-l";
            public static final String XLARGE = "procoeton-ui-px-xl";
        }

        public static final class Vertical {
            private Vertical() {
            }

            public static final String NONE = "procoeton-ui-py-0";
            public static final String XXSMALL = "procoeton-ui-py-xxs";
            public static final String XSMALL = "procoeton-ui-py-xs";
            public static final String SMALL = "procoeton-ui-py-s";
            public static final String MEDIUM = "procoeton-ui-py-m";
            public static final String LARGE = "procoeton-ui-py-l";
            public static final String XLARGE = "procoeton-ui-py-xl";
        }

        public static final class Top {
            private Top() {
            }

            public static final String NONE = "procoeton-ui-pt-0";
            public static final String XXSMALL = "procoeton-ui-pt-xxs";
            public static final String XSMALL = "procoeton-ui-pt-xs";
            public static final String SMALL = "procoeton-ui-pt-s";
            public static final String MEDIUM = "procoeton-ui-pt-m";
            public static final String LARGE = "procoeton-ui-pt-l";
            public static final String XLARGE = "procoeton-ui-pt-xl";
        }

        public static final class Bottom {
            private Bottom() {
            }

            public static final String NONE = "procoeton-ui-pb-0";
            public static final String XXSMALL = "procoeton-ui-pb-xxs";
            public static final String XSMALL = "procoeton-ui-pb-xs";
            public static final String SMALL = "procoeton-ui-pb-s";
            public static final String MEDIUM = "procoeton-ui-pb-m";
            public static final String LARGE = "procoeton-ui-pb-l";
            public static final String XLARGE = "procoeton-ui-pb-xl";
        }

        public static final class Left {
            private Left() {
            }

            public static final String NONE = "procoeton-ui-pl-0";
            public static final String XXSMALL = "procoeton-ui-pl-xxs";
            public static final String XSMALL = "procoeton-ui-pl-xs";
            public static final String SMALL = "procoeton-ui-pl-s";
            public static final String MEDIUM = "procoeton-ui-pl-m";
            public static final String LARGE = "procoeton-ui-pl-l";
            public static final String XLARGE = "procoeton-ui-pl-xl";
        }

        public static final class Right {
            private Right() {
            }

            public static final String NONE = "procoeton-ui-pr-0";
            public static final String XXSMALL = "procoeton-ui-pr-xxs";
            public static final String XSMALL = "procoeton-ui-pr-xs";
            public static final String SMALL = "procoeton-ui-pr-s";
            public static final String MEDIUM = "procoeton-ui-pr-m";
            public static final String LARGE = "procoeton-ui-pr-l";
            public static final String XLARGE = "procoeton-ui-pr-xl";
        }
    }

  /* =========================
     Margin
     ========================= */

    public static final class Margin {
        private Margin() {
        }

        public static final String NONE = "procoeton-ui-m-0";
        public static final String XXSMALL = "procoeton-ui-m-xxs";
        public static final String XSMALL = "procoeton-ui-m-xs";
        public static final String SMALL = "procoeton-ui-m-s";
        public static final String MEDIUM = "procoeton-ui-m-m";
        public static final String LARGE = "procoeton-ui-m-l";
        public static final String XLARGE = "procoeton-ui-m-xl";
        public static final String AUTO = "procoeton-ui-m-auto";

        public static final class Horizontal {
            private Horizontal() {
            }

            public static final String NONE = "procoeton-ui-mx-0";
            public static final String XXSMALL = "procoeton-ui-mx-xxs";
            public static final String XSMALL = "procoeton-ui-mx-xs";
            public static final String SMALL = "procoeton-ui-mx-s";
            public static final String MEDIUM = "procoeton-ui-mx-m";
            public static final String LARGE = "procoeton-ui-mx-l";
            public static final String XLARGE = "procoeton-ui-mx-xl";
            public static final String AUTO = "procoeton-ui-mx-auto";
        }

        public static final class Vertical {
            private Vertical() {
            }

            public static final String NONE = "procoeton-ui-my-0";
            public static final String XXSMALL = "procoeton-ui-my-xxs";
            public static final String XSMALL = "procoeton-ui-my-xs";
            public static final String SMALL = "procoeton-ui-my-s";
            public static final String MEDIUM = "procoeton-ui-my-m";
            public static final String LARGE = "procoeton-ui-my-l";
            public static final String XLARGE = "procoeton-ui-my-xl";
            public static final String AUTO = "procoeton-ui-my-auto";
        }

        public static final class Top {
            private Top() {
            }

            public static final String NONE = "procoeton-ui-mt-0";
            public static final String XXSMALL = "procoeton-ui-mt-xxs";
            public static final String XSMALL = "procoeton-ui-mt-xs";
            public static final String SMALL = "procoeton-ui-mt-s";
            public static final String MEDIUM = "procoeton-ui-mt-m";
            public static final String LARGE = "procoeton-ui-mt-l";
            public static final String XLARGE = "procoeton-ui-mt-xl";
            public static final String AUTO = "procoeton-ui-mt-auto";
        }

        public static final class Bottom {
            private Bottom() {
            }

            public static final String NONE = "procoeton-ui-mb-0";
            public static final String XXSMALL = "procoeton-ui-mb-xxs";
            public static final String XSMALL = "procoeton-ui-mb-xs";
            public static final String SMALL = "procoeton-ui-mb-s";
            public static final String MEDIUM = "procoeton-ui-mb-m";
            public static final String LARGE = "procoeton-ui-mb-l";
            public static final String XLARGE = "procoeton-ui-mb-xl";
            public static final String AUTO = "procoeton-ui-mb-auto";
        }

        public static final class Left {
            private Left() {
            }

            public static final String NONE = "procoeton-ui-ml-0";
            public static final String XXSMALL = "procoeton-ui-ml-xxs";
            public static final String XSMALL = "procoeton-ui-ml-xs";
            public static final String SMALL = "procoeton-ui-ml-s";
            public static final String MEDIUM = "procoeton-ui-ml-m";
            public static final String LARGE = "procoeton-ui-ml-l";
            public static final String XLARGE = "procoeton-ui-ml-xl";
            public static final String AUTO = "procoeton-ui-ml-auto";
        }

        public static final class Right {
            private Right() {
            }

            public static final String NONE = "procoeton-ui-mr-0";
            public static final String XXSMALL = "procoeton-ui-mr-xxs";
            public static final String XSMALL = "procoeton-ui-mr-xs";
            public static final String SMALL = "procoeton-ui-mr-s";
            public static final String MEDIUM = "procoeton-ui-mr-m";
            public static final String LARGE = "procoeton-ui-mr-l";
            public static final String XLARGE = "procoeton-ui-mr-xl";
            public static final String AUTO = "procoeton-ui-mr-auto";
        }
    }

  /* =========================
     Font size / weight
     ========================= */

    public static final class FontSize {
        private FontSize() {
        }

        public static final String XXSMALL = "procoeton-ui-text-xxs";
        public static final String XSMALL = "procoeton-ui-text-xs";
        public static final String SMALL = "procoeton-ui-text-s";
        public static final String MEDIUM = "procoeton-ui-text-m";
        public static final String LARGE = "procoeton-ui-text-l";
        public static final String XLARGE = "procoeton-ui-text-xl";
        public static final String XXLARGE = "procoeton-ui-text-xxl";
        public static final String XXXLARGE = "procoeton-ui-text-xxxl";
    }

    public static final class FontWeight {
        private FontWeight() {
        }

        public static final String LIGHT = "procoeton-ui-font-light";
        public static final String NORMAL = "procoeton-ui-font-normal";
        public static final String MEDIUM = "procoeton-ui-font-medium";
        public static final String SEMIBOLD = "procoeton-ui-font-semibold";
        public static final String BOLD = "procoeton-ui-font-bold";
    }

  /* =========================
     Text color
     ========================= */

    public static final class TextColor {
        private TextColor() {
        }

        public static final String PRIMARY = "procoeton-ui-text-primary";
        public static final String SECONDARY = "procoeton-ui-text-secondary";
        public static final String TERTIARY = "procoeton-ui-text-tertiary";
        public static final String SUCCESS = "procoeton-ui-text-success";
        public static final String ERROR = "procoeton-ui-text-error";
        public static final String WARNING = "procoeton-ui-text-warning";
    }

  /* =========================
     Border / BorderColor / BorderRadius
     ========================= */

    public static final class Border {
        private Border() {
        }

        public static final String NONE = "procoeton-ui-border-0";
        public static final String XXSMALL = "procoeton-ui-border-xxs";
        public static final String XSMALL = "procoeton-ui-border-xs";
        public static final String SMALL = "procoeton-ui-border-s";
        public static final String MEDIUM = "procoeton-ui-border-m";
        public static final String LARGE = "procoeton-ui-border-l";
        public static final String XLARGE = "procoeton-ui-border-xl";

        public static final String SOLID = "procoeton-ui-border-solid";
        public static final String DASHED = "procoeton-ui-border-dashed";
        public static final String DOTTED = "procoeton-ui-border-dotted";
    }

    public static final class BorderColor {
        private BorderColor() {
        }

        public static final String TRANSPARENT = "procoeton-ui-border-color-transparent";
        public static final String BASE = "procoeton-ui-border-color-base";
        public static final String CONTRAST = "procoeton-ui-border-color-contrast";
        public static final String PRIMARY = "procoeton-ui-border-color-primary";
        public static final String SUCCESS = "procoeton-ui-border-color-success";
        public static final String ERROR = "procoeton-ui-border-color-error";
        public static final String WARNING = "procoeton-ui-border-color-warning";
    }

    public static final class BorderRadius {
        private BorderRadius() {
        }

        public static final String SMALL = "procoeton-ui-radius-s";
        public static final String MEDIUM = "procoeton-ui-radius-m";
        public static final String LARGE = "procoeton-ui-radius-l";
    }

  /* =========================
     Overflow
     ========================= */

    public static final class Overflow {
        private Overflow() {
        }

        public static final String VISIBLE = "procoeton-ui-overflow-visible";
        public static final String HIDDEN = "procoeton-ui-overflow-hidden";
        public static final String AUTO = "procoeton-ui-overflow-auto";
        public static final String SCROLL = "procoeton-ui-overflow-scroll";
    }

  /* =========================
     Badge (Aura-friendly)
     ========================= */

    public static final class Badge {
        private Badge() {
        }

        /**
         * Base badge style
         */
        public static final String BASE = "procoeton-ui-badge";

        /**
         * Size
         */
        public static final String SMALL = "procoeton-ui-badge--small";

        /**
         * Variants
         */
        public static final String DEFAULT = "procoeton-ui-badge--default";
        public static final String SUCCESS = "procoeton-ui-badge--success";
        public static final String WARNING = "procoeton-ui-badge--warning";
        public static final String ERROR = "procoeton-ui-badge--error";
        public static final String CONTRAST = "procoeton-ui-badge--contrast";

        /**
         * Emphasis
         */
        public static final String PRIMARY = "procoeton-ui-badge--primary";
    }

  /* =========================
     Misc
     ========================= */

    public static final class Misc {
        private Misc() {
        }

        public static final String TRUNCATE = "procoeton-ui-truncate";
        public static final String POINTER = "procoeton-ui-pointer";
        public static final String SELECT_NONE = "procoeton-ui-select-none";
    }
}