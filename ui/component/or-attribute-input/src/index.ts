import {
    css,
    customElement,
    html,
    LitElement,
    property,
    PropertyValues,
    query,
    TemplateResult,
    unsafeCSS
} from "lit-element";
import {ifDefined} from "lit-html/directives/if-defined";
import {i18next, translate} from "@openremote/or-translate";
import {
    Attribute,
    AttributeDescriptor,
    AttributeEvent,
    AttributeRef,
    AttributeValueDescriptor,
    AttributeValueType,
    MetaItemType,
    SharedEvent,
    ValueType
} from "@openremote/model";
import manager, {AssetModelUtil, DefaultColor4, subscribe, Util} from "@openremote/core";
import "@openremote/or-input";
import {InputType, OrInput, OrInputChangedEvent} from "@openremote/or-input";
import "@openremote/or-map";
import {showDialog, OrMwcDialog} from "@openremote/or-mwc-components/dist/or-mwc-dialog";
import {
    getMarkerIconAndColorFromAssetType,
    LngLat,
    MapEventDetail,
    MapGL,
    OrMapClickedEvent,
    Util as MapUtil,
    OrMap,
    OrMapMarker
} from "@openremote/or-map";

export class OrAttributeInputChangedEvent extends CustomEvent<OrAttributeInputChangedEventDetail> {

    public static readonly NAME = "or-attribute-input-changed";

    constructor(value?: any, previousValue?: any) {
        super(OrAttributeInputChangedEvent.NAME, {
            detail: {
                value: value,
                previousValue: previousValue
            },
            bubbles: true,
            composed: true
        });
    }
}

export interface OrAttributeInputChangedEventDetail {
    value?: any;
    previousValue?: any;
}

declare global {
    export interface HTMLElementEventMap {
        [OrAttributeInputChangedEvent.NAME]: OrAttributeInputChangedEvent;
    }
}

export type AttributeInputCustomProviderResult = ((value: any, timestamp: number | undefined, loading: boolean, sending: boolean, error: boolean) => TemplateResult) | undefined;

export type AttributeInputCustomProvider = (assetType: string | undefined, attribute: Attribute | undefined, attributeDescriptor: AttributeDescriptor | undefined, valueDescriptor: AttributeValueDescriptor | undefined, valueChangeNotifier: (value: any | undefined) => void, attributeInput: OrAttributeInput) => AttributeInputCustomProviderResult;

export class CenterControl {
    protected map?: MapGL;
    protected elem?: HTMLElement;
    public pos?: LngLat;

    onAdd(map: MapGL): HTMLElement {
        this.map = map;
        const control = document.createElement("div");
        control.classList.add("mapboxgl-ctrl");
        control.classList.add("mapboxgl-ctrl-group");
        const button = document.createElement("button");
        button.className = "mapboxgl-ctrl-icon mapboxgl-ctrl-geolocate";
        button.addEventListener("click", (ev) => map.flyTo({
            center: this.pos,
            zoom: map.getZoom()
        }));
        control.appendChild(button);
        this.elem = control;
        return control;
    }

    onRemove(map: MapGL) {
        this.map = undefined;
        this.elem = undefined;
    }
}

const CoordinatesRegexPattern = "^[ ]*(?:Lat: )?(-?\\d+\\.?\\d*)[, ]+(?:Lng: )?(-?\\d+\\.?\\d*)[ ]*$";

function getCoordinatesInputKeyHandler(valueChangedHandler: (value: LngLat | undefined) => void) {
    return (e: KeyboardEvent) => {
        if (e.code === "Enter" || e.code === "NumpadEnter") {
            const valStr = (e.target as OrInput).value as string;
            let value: LngLat | undefined;

            if (valStr) {
                const lngLatArr = valStr.split(/[ ,]/).filter(v => !!v);
                if (lngLatArr.length === 2) {
                    value = new LngLat(
                        Number.parseFloat(lngLatArr[0]),
                        Number.parseFloat(lngLatArr[1])
                    );
                }
            }
            valueChangedHandler(value);
        }
    };
}

export class CoordinatesControl {

    protected map?: MapGL;
    protected elem?: HTMLElement;
    protected input!: OrInput;
    protected _readonly = false;
    protected _value: any;
    protected _valueChangedHandler: (value: LngLat | undefined) => void;

    constructor(disabled: boolean = false, valueChangedHandler: (value: LngLat | undefined) => void) {
        this._readonly = disabled;
        this._valueChangedHandler = valueChangedHandler;
    }

    onAdd(map: MapGL): HTMLElement {
        this.map = map;
        const control = document.createElement("div");
        control.classList.add("mapboxgl-ctrl");
        control.classList.add("mapboxgl-ctrl-group");

        const input = new OrInput();
        input.type = InputType.TEXT;
        input.outlined = true;
        input.compact = true;
        input.readonly = this._readonly;
        input.icon = "crosshairs-gps";
        input.value = this._value;
        input.pattern = CoordinatesRegexPattern;
        input.onkeyup = getCoordinatesInputKeyHandler(this._valueChangedHandler);

        control.appendChild(input);
        this.elem = control;
        this.input = input;
        return control;
    }

    onRemove(map: MapGL) {
        this.map = undefined;
        this.elem = undefined;
    }

    public set readonly(readonly: boolean) {
        this._readonly = readonly;
        if (this.input) {
            this.input.readonly = readonly;
        }
    }

    public set value(value: any) {
        this._value = value;
        if (this.input) {
            this.input.value = value;
        }
    }
}

export const GeoJsonPointInputTemplateProvider: AttributeInputCustomProvider = (assetType, attribute, attributeDescriptor, valueDescriptor, valueChangeNotifier, attributeInput) => {

    const centerControl = new CenterControl();
    const coordinatesControl = new CoordinatesControl(attributeInput.disabled, valueChangeNotifier);

    return (value, timestamp, loading, sending, error) => {
        let pos: LngLat | undefined;
        let center: number[] | undefined;

        if (value) {
            pos = MapUtil.getLngLat(value);
            center = pos ? pos.toArray() : undefined;
        }

        const centerStr = center ? center.join(", ") : undefined;
        centerControl.pos = pos;
        coordinatesControl.readonly = !!attributeInput.disabled || !!attributeInput.readonly || sending || loading;
        coordinatesControl.value = centerStr;

        const iconAndColor = getMarkerIconAndColorFromAssetType(assetType);

        let dialog: OrMwcDialog | undefined;

        const updateHandler = () => {
            if (valueChangeNotifier) {
                valueChangeNotifier(MapUtil.getGeoJSONPoint(pos));
            }
        };

        const setPos = (lngLat: LngLat | undefined) => {
            if (attributeInput.readonly || attributeInput.disabled) {
                return;
            }

            pos = lngLat;

            if (dialog) {
                // We're in compact mode modal
                const marker = dialog.shadowRoot!.getElementById("geo-json-point-marker") as OrMapMarker;
                marker.lng = pos ? pos.lng : undefined;
                marker.lat = pos ? pos.lat : undefined;
                center = pos ? pos.toArray() : undefined;
                const centerStr = center ? center.join(", ") : undefined;
                coordinatesControl.value = centerStr;
            } else {
                updateHandler();
            }
        };

        let content = html`
            <style>
                or-map {
                    border: #e5e5e5 1px solid;
                    margin: 3px 0;
                }
            </style>
            <or-map id="geo-json-point-map" class="or-map" @or-map-clicked="${(ev: OrMapClickedEvent) => {if (ev.detail.doubleClick) {setPos(ev.detail.lngLat);}}}" .center="${center}" .controls="${[centerControl, [coordinatesControl, "top-left"]]}">
                <or-map-marker id="geo-json-point-marker" active .lng="${pos ? pos.lng : undefined}" .lat="${pos ? pos.lat : undefined}" .icon="${iconAndColor ? iconAndColor.icon : undefined}" .activeColor="${iconAndColor ? "#" + iconAndColor.color : undefined}" .color="${iconAndColor ? "#" + iconAndColor.color : undefined}"></or-map-marker>
            </or-map>
        `;

        if (attributeInput.compact) {
            const mapContent = content;

            const onClick = () => {
                dialog = showDialog(
                    {
                        content: mapContent,
                        styles: html`
                            <style>
                                or-map {
                                    width: 600px !important;
                                    height: 600px !important;
                                }
                            </style>
                        `,
                        actions: [
                            {
                                actionName: "none",
                                content: i18next.t("none"),
                                action: () => {
                                    setPos(undefined);
                                    updateHandler();
                                }
                            },
                            {
                                actionName: "ok",
                                content: i18next.t("ok"),
                                action: () => {
                                    updateHandler();
                                }
                            },
                            {
                                default: true,
                                actionName: "cancel",
                                content: i18next.t("cancel")
                            }
                        ]
                    });
            };

            content = html`
                <style>
                    #geo-json-point-input-compact-wrapper {
                        display: table-cell;
                    }
                    #geo-json-point-input-compact-wrapper > * {
                        vertical-align: middle;
                    }
                </style>
                <div id="geo-json-point-input-compact-wrapper">
                    <or-input .type="${InputType.TEXT}" .value="${centerStr}" .pattern="${CoordinatesRegexPattern}" @keyup="${(e: KeyboardEvent) => getCoordinatesInputKeyHandler(valueChangeNotifier)(e)}"></or-input>
                    <or-input style="width: auto;" .type="${InputType.BUTTON}" compact action icon="crosshairs-gps" @click="${onClick}"></or-input>
                </div>
            `;
        }

        return getAttributeInputWrapper(content, loading, !!attributeInput.disabled, attributeInput.hasHelperText ? getHelperText(sending, false, timestamp) : undefined, attributeInput.label, undefined);
    }
}

export function getAttributeInputWrapper(content: TemplateResult, loading: boolean, disabled: boolean, helperText: string | undefined, label: string | undefined, buttonIcon?: string, valueProvider?: () => any, valueChangeConsumer?: (value: any) => void): TemplateResult {

    if (helperText) {
        content = html`
                    <div id="wrapper-helper">
                        ${label ? html`<div style="margin-left: 16px">${label}</div>` : ``}
                        <div id="wrapper-input">${content}</div>
                        <div id="helper-text">${helperText}</div>
                    </div>
                `;
    }

    if (buttonIcon) {
        content = html`
                ${content}
                <or-input id="send-btn" icon="${buttonIcon}" type="button" .disabled="${disabled || loading}" @or-input-changed="${(e: OrInputChangedEvent) => {
            e.stopPropagation();
            if (valueProvider && valueChangeConsumer) {
                valueChangeConsumer(valueProvider())
            }
        }}"></or-input>
            `;
    }

    return html`
            <div id="wrapper" class="${buttonIcon === undefined || buttonIcon ? "no-padding" : "right-padding"}">
                ${content}
                <div id="scrim" class="${ifDefined(loading ? undefined : "hidden")}"><progress class="pure-material-progress-circular"></progress></div>
            </div>
        `;
}

export function getHelperText(sending: boolean, error: boolean, timestamp: number | undefined): string | undefined {
    if (sending) {
        return i18next.t("sending");
    }

    if (error) {
        return i18next.t("sendFailed");
    }

    if (!timestamp) {
        return;
    }

    return i18next.t("updatedWithDate", { date: new Date(timestamp) });
}

const DEFAULT_TIMEOUT = 5000;

// TODO: Add support for attribute not found and attribute deletion/addition
@customElement("or-attribute-input")
export class OrAttributeInput extends subscribe(manager)(translate(i18next)(LitElement)) {

    // language=CSS
    static get styles() {
        return css`
            :host {
                display: inline-block;
            }
            
            #wrapper or-input, #wrapper or-map {
                width: 100%;
            }
            
            #wrapper or-map {
                min-height: 250px;
            }
            
            #wrapper {
                display: flex;
                position: relative;
            }
            
            #wrapper.right-padding {
                padding-right: 48px;
            }
            
            #wrapper-helper {
                display: flex;
                flex: 1;
                flex-direction: column;
            }
            
            #wrapper-input {
                flex: 1;
                display: flex;
            }
            
            #wrapper-input > or-input {
                margin-left: 16px;
            }
            
            /* Copy of mdc text field helper text styles */
            #helper-text {
                margin-left: 16px;
                min-width: 255px;
                color: rgba(0, 0, 0, 0.6);
                font-family: Roboto, sans-serif;
                -webkit-font-smoothing: antialiased;
                font-size: 0.75rem;
                font-weight: 400;
                letter-spacing: 0.0333333em;
            }
            
            #scrim {
                position: absolute;
                left: 0;
                top: 0;
                right: 0;
                bottom: 0;
                background: white;
                opacity: 0.2;
                display: flex;
                align-items: center;
                justify-content: center;
            }
            
            #scrim.hidden {
                display: none;
            }

            #send-btn { 
                flex: 0;
            }
            
            /*  https://codepen.io/finnhvman/pen/bmNdNr  */
            .pure-material-progress-circular {
                -webkit-appearance: none;
                -moz-appearance: none;
                appearance: none;
                box-sizing: border-box;
                border: none;
                border-radius: 50%;
                padding: 0.25em;
                width: 3em;
                height: 3em;
                color: var(--or-app-color4, ${unsafeCSS(DefaultColor4)});
                background-color: transparent;
                font-size: 16px;
                overflow: hidden;
            }

            .pure-material-progress-circular::-webkit-progress-bar {
                background-color: transparent;
            }

            /* Indeterminate */
            .pure-material-progress-circular:indeterminate {
                -webkit-mask-image: linear-gradient(transparent 50%, black 50%), linear-gradient(to right, transparent 50%, black 50%);
                mask-image: linear-gradient(transparent 50%, black 50%), linear-gradient(to right, transparent 50%, black 50%);
                animation: pure-material-progress-circular 6s infinite cubic-bezier(0.3, 0.6, 1, 1);
            }

            :-ms-lang(x), .pure-material-progress-circular:indeterminate {
                animation: none;
            }

            .pure-material-progress-circular:indeterminate::before,
            .pure-material-progress-circular:indeterminate::-webkit-progress-value {
                content: "";
                display: block;
                box-sizing: border-box;
                margin-bottom: 0.25em;
                border: solid 0.25em transparent;
                border-top-color: currentColor;
                border-radius: 50%;
                width: 100% !important;
                height: 100%;
                background-color: transparent;
                animation: pure-material-progress-circular-pseudo 0.75s infinite linear alternate;
            }

            .pure-material-progress-circular:indeterminate::-moz-progress-bar {
                box-sizing: border-box;
                border: solid 0.25em transparent;
                border-top-color: currentColor;
                border-radius: 50%;
                width: 100%;
                height: 100%;
                background-color: transparent;
                animation: pure-material-progress-circular-pseudo 0.75s infinite linear alternate;
            }

            .pure-material-progress-circular:indeterminate::-ms-fill {
                animation-name: -ms-ring;
            }

            @keyframes pure-material-progress-circular {
                0% {
                    transform: rotate(0deg);
                }
                12.5% {
                    transform: rotate(180deg);
                    animation-timing-function: linear;
                }
                25% {
                    transform: rotate(630deg);
                }
                37.5% {
                    transform: rotate(810deg);
                    animation-timing-function: linear;
                }
                50% {
                    transform: rotate(1260deg);
                }
                62.5% {
                    transform: rotate(1440deg);
                    animation-timing-function: linear;
                }
                75% {
                    transform: rotate(1890deg);
                }
                87.5% {
                    transform: rotate(2070deg);
                    animation-timing-function: linear;
                }
                100% {
                    transform: rotate(2520deg);
                }
            }

            @keyframes pure-material-progress-circular-pseudo {
                0% {
                    transform: rotate(-30deg);
                }
                29.4% {
                    border-left-color: transparent;
                }
                29.41% {
                    border-left-color: currentColor;
                }
                64.7% {
                    border-bottom-color: transparent;
                }
                64.71% {
                    border-bottom-color: currentColor;
                }
                100% {
                    border-left-color: currentColor;
                    border-bottom-color: currentColor;
                    transform: rotate(225deg);
                }
            }
        `;
    }

    @property({type: Object, reflect: false})
    public attribute?: Attribute;

    @property({type: Object})
    public attributeRef?: AttributeRef;

    @property({type: Object})
    public attributeDescriptor?: AttributeDescriptor;

    @property({type: Object})
    public attributeValueDescriptor?: AttributeValueDescriptor;

    @property({type: String})
    public assetType?: string;

    @property({type: String})
    public label?: string;

    @property({type: Boolean})
    public disabled?: boolean;

    @property({type: Boolean})
    public readonly?: boolean;

    @property()
    public value?: any;

    @property()
    public inputType?: InputType;

    @property({type: Boolean})
    public hasHelperText?: boolean;

    @property({type: Boolean})
    public disableButton?: boolean;

    @property({type: Boolean})
    public disableSubscribe: boolean = false;

    @property({type: Boolean})
    public disableWrite: boolean = false;

    @property({type: Boolean})
    public compact: boolean = false;

    @property()
    protected _attributeEvent?: AttributeEvent;

    @property()
    protected _writeTimeoutHandler?: number;

    @query("#input")
    protected _attrInput!: OrInput;
    @query("#send-btn")
    protected _sendButton!: OrInput;
    @query("#scrim")
    protected _scrimElem!: HTMLDivElement;

    public customProvider?: AttributeInputCustomProvider;
    public writeTimeout?: number = DEFAULT_TIMEOUT;
    protected _template?: AttributeInputCustomProviderResult;
    protected _attributeDescriptor?: AttributeDescriptor;
    protected _attributeValueDescriptor?: AttributeValueDescriptor;
    protected _inputType?: InputType;
    protected _step?: number;
    protected _min?: any;
    protected _max?: any;
    protected _unit?: string;
    protected _options?: any;
    protected _showButton?: boolean;
    protected _valueFormat?: string;
    protected _sendError = false;

    public disconnectedCallback() {
        super.disconnectedCallback();
        this._clearWriteTimeout();
    }

    public shouldUpdate(_changedProperties: PropertyValues): boolean {
        const shouldUpdate = super.shouldUpdate(_changedProperties);

        let updateSubscribedRefs = false;
        let updateDescriptors = false;

        if (_changedProperties.has("disableSubscribe")) {
            updateSubscribedRefs = true;
        }

        if (_changedProperties.has("attributeDescriptor")
            || _changedProperties.has("attributeValueDescriptor")
            || _changedProperties.has("assetType")) {
            updateDescriptors = true;
        }

        if (_changedProperties.has("attribute")) {
            const oldAttr = {..._changedProperties.get("attribute") as Attribute};
            const attr = this.attribute;

            if (oldAttr && attr) {
                const oldValue = oldAttr.value;
                const oldTimestamp = oldAttr.valueTimestamp;

                // Compare attributes ignoring the timestamp and value
                oldAttr.value = attr.value;
                oldAttr.valueTimestamp = attr.valueTimestamp;
                if (Util.objectsEqual(oldAttr, attr)) {
                    // Compare value and timestamp
                    if (!Util.objectsEqual(oldValue, attr.value) || oldTimestamp !== attr.valueTimestamp) {
                        this._onAttributeValueChanged(oldValue, attr.value, attr.valueTimestamp);
                    } else if (_changedProperties.size === 1) {
                        // Only the attribute has 'changed' and we've handled it so don't perform update
                        return false;
                    }
                } else {
                    updateSubscribedRefs = true;
                    updateDescriptors = true;
                }
            }
        }

        if (_changedProperties.has("attributeRef") && !Util.objectsEqual(_changedProperties.get("attributeRef"), this.attributeRef)) {
            updateSubscribedRefs = true;
            updateDescriptors = true;
        }

        if (updateDescriptors) {
            this._updateDescriptors();
        }

        if (updateSubscribedRefs) {
            this._updateSubscribedRefs();
        }

        if (this._template
            && (_changedProperties.has("disabled")
                || _changedProperties.has("readonly")
                || _changedProperties.has("label"))) {
            this._updateTemplate();
        }

        return shouldUpdate;
    }

    protected _updateSubscribedRefs(): void {
        this._attributeEvent = undefined;

        if (this.disableSubscribe) {
            this.attributeRefs = undefined;
        } else {
            const attributeRef = this._getAttributeRef();
            this.attributeRefs = attributeRef ? [attributeRef] : undefined;
        }
    }

    protected _getAttributeRef(): AttributeRef | undefined {
        if (this.attributeRef) {
            return this.attributeRef;
        }
        if (this.attribute) {
            return {
                assetId: this.attribute.assetId!,
                attributeName: this.attribute.name!
            }
        }
    }

    protected _updateDescriptors(): void {

        this._attributeValueDescriptor = undefined;
        this._attributeDescriptor = undefined;

        if (this.attributeDescriptor && this.attributeValueDescriptor) {
            this._attributeDescriptor = this.attributeDescriptor;
            this._attributeValueDescriptor = this.attributeValueDescriptor;
        } else {
            const attributeOrDescriptorOrName = this.attributeDescriptor || (this.attribute ? this.attribute : this.attributeRef ? this.attributeRef.attributeName! : undefined);

            if (!attributeOrDescriptorOrName) {
                this._attributeDescriptor = this.attributeDescriptor;
                this._attributeValueDescriptor = this.attributeValueDescriptor;
            } else {
                const attributeAndValueDescriptors = AssetModelUtil.getAttributeAndValueDescriptors(this.assetType, attributeOrDescriptorOrName);
                this._attributeDescriptor = attributeAndValueDescriptors[0];
                this._attributeValueDescriptor = this.attributeValueDescriptor ? this._attributeValueDescriptor : attributeAndValueDescriptors[1];
            }
        }

        this._updateTemplate();
    }

    protected _updateTemplate(): void {
        this._template = undefined;
        this._inputType = undefined;
        this._step = undefined;
        this._min = undefined;
        this._max = undefined;
        this._unit = undefined;
        this._options = undefined;
        this._showButton = undefined;
        this._inputType = undefined;
        this._valueFormat = undefined;

        if (this.customProvider) {
            this._template = this.customProvider(this.assetType, this.attribute, this._attributeDescriptor, this._attributeValueDescriptor, (v) => this._updateValue(v), this);
        }

        if (this._template) {
            return;
        }

        if (this.inputType) {
            this._inputType = this.inputType;
        } else if (this._attributeValueDescriptor) {
            switch (this._attributeValueDescriptor.name) {
                case ValueType.GEO_JSON_POINT.name:
                    this._template = GeoJsonPointInputTemplateProvider(this.assetType, this.attribute, this._attributeDescriptor, this._attributeValueDescriptor, (v) => this._updateValue(v), this);
                    return;
                case ValueType.SWITCH_MOMENTARY.name:
                    this._inputType = InputType.BUTTON_MOMENTARY;
                    break;
                default:
                    // Use value type
                    switch (this._attributeValueDescriptor.valueType) {
                        case ValueType.STRING:
                            this._inputType = InputType.TEXT;
                            break;
                        case ValueType.NUMBER:
                            this._inputType = InputType.NUMBER;
                            break;
                        case ValueType.BOOLEAN:
                            this._inputType = InputType.SWITCH;
                            break;
                        default:
                            this._inputType = InputType.JSON;
                            break;
                    }
                    break;
            }
        }

        if (!this._inputType && (this.attribute || this.value)) {
            const currentValue = this.attribute ? this.attribute.value : this.value;

            if (currentValue !== undefined && currentValue !== null) {
                if (typeof currentValue === "number") {
                    this._inputType = InputType.NUMBER;
                } else if (typeof currentValue === "string") {
                    this._inputType = InputType.TEXT;
                } else if (typeof currentValue === "boolean") {
                    this._inputType = InputType.SWITCH;
                } else {
                    this._inputType = InputType.JSON;
                }
            }
        }

        if (this._inputType) {
            this._min = Util.getMetaValue(MetaItemType.RANGE_MIN, this.attribute, this._attributeDescriptor, this._attributeValueDescriptor) as number;
            this._max = Util.getMetaValue(MetaItemType.RANGE_MAX, this.attribute, this._attributeDescriptor, this._attributeValueDescriptor) as number;
            this._unit = Util.getMetaValue(MetaItemType.UNIT_TYPE, this.attribute, this._attributeDescriptor, this._attributeValueDescriptor) as string;
            this._step = Util.getMetaValue(MetaItemType.STEP, this.attribute, this._attributeDescriptor, this._attributeValueDescriptor) as number;
            this._options = Util.getMetaValue(MetaItemType.ALLOWED_VALUES, this.attribute, this._attributeDescriptor);

            if (this._inputType === InputType.TEXT) {
                if (this._options && Array.isArray(this._options) && this._options.length > 0) {
                    this._inputType = InputType.SELECT;
                } else if (Util.getMetaValue(MetaItemType.MULTILINE, this.attribute, this._attributeDescriptor, this._attributeValueDescriptor)) {
                    this._inputType = InputType.TEXTAREA;
                }
            }

            if (this._inputType === InputType.NUMBER) {
                if (this._options && Array.isArray(this._options) && this._options.length > 0) {
                    this._inputType = InputType.SELECT;
                    this._options = this._options.map((opt, index) => [index, opt]);
                } else if (this._min !== undefined && this._max) {
                    this._inputType = InputType.RANGE;
                }
            }

            this._valueFormat = Util.getAttributeValueFormat(this.attribute, this._attributeDescriptor, this._attributeValueDescriptor);
            this._showButton = !this.isReadonly() && !this.disabled && !this.disableButton && this.inputTypeSupportsButton() && !!this._getAttributeRef();
        }
    }

    public getLabel(): string | undefined {
        let label;

        if (this.label) {
            label = this.label;
        } else if (this.label !== "" && this.label !== null) {
            const descriptors = AssetModelUtil.getAttributeAndValueDescriptors(this.assetType, this.attribute || this._attributeDescriptor);
            label = Util.getAttributeLabel(this.attribute, descriptors[0], descriptors[1], true);
        }

        return label;
    }

    public isReadonly(): boolean {
        return this.readonly !== undefined ? this.readonly : Util.getMetaValue(MetaItemType.READ_ONLY, this.attribute, this._attributeDescriptor);
    }

    public render() {

        // Check if attribute hasn't been loaded yet or pending write
        const loading = (this.attributeRefs && !this._attributeEvent) || !!this._writeTimeoutHandler;
        let content: TemplateResult | string | undefined = "";

        const value = this.getValue();

        if (this._template) {
            content = this._template(value, this.getTimestamp(), loading, !!this._writeTimeoutHandler, this._sendError);
        } else {

            if (!this._inputType) {
                content = html`<div><or-translate .value="attributeUnsupported"></or-translate></div>`;
            }

            const helperText = this.hasHelperText ? getHelperText(!!this._writeTimeoutHandler, this._sendError, this.getTimestamp()) : undefined;
            const buttonIcon = this._writeTimeoutHandler ? "send-clock" : "send";
            const supportsHelperText = this.inputTypeSupportsHelperText();
            let label = this.getLabel();

            if (helperText && !this.inputTypeSupportsHelperText()) {
                label = undefined;
            }

            content = html`<or-input id="input" .type="${this._inputType}" .label="${label}" .value="${value}" 
                .allowedValues="${this._options}" .min="${this._min}" .max="${this._max}" .format="${this._valueFormat}"
                .options="${this._options}" .readonly="${this.isReadonly()}" .disabled="${this.disabled || loading}" 
                .helperText="${supportsHelperText ? helperText : undefined}" .helperPersistent="${true}"
                @keyup="${(e: KeyboardEvent) => {
                    if ((e.code === "Enter" || e.code === "NumpadEnter") && this._inputType !== InputType.JSON && this._inputType !== InputType.TEXTAREA) {
                        this._updateValue(this._attrInput.value);
                    }
                }}" @or-input-changed="${(e: OrInputChangedEvent) => {
                    e.stopPropagation();
                    if (!this._showButton) {
                        this._updateValue(e.detail.value);
                    }
                }}"></or-input>`;

            content = getAttributeInputWrapper(content, loading, !!this.disabled, !supportsHelperText ? helperText : undefined, this.getLabel(), this._showButton ? buttonIcon : this.disableButton ? undefined : "", () => this._attrInput.value, (v) => this._updateValue(v));
        }

        return content;
    }

    protected updated(_changedProperties: PropertyValues): void {
        if (_changedProperties.has("_writeTimeoutHandler") && !this._writeTimeoutHandler) {
            if (this._attrInput) {
                this._attrInput.focus();
            }
        }
    }

    protected getValue(): any {
        return this._attributeEvent ? this._attributeEvent.attributeState!.value : this.attribute ? this.attribute.value : this.value;
    }

    protected getTimestamp(): number | undefined {
        return this._attributeEvent ? this._attributeEvent.timestamp : this.attribute ? this.attribute.valueTimestamp : undefined;
    }

    protected inputTypeSupportsButton() {
        return this._inputType === InputType.NUMBER
            || this._inputType === InputType.TELEPHONE
            || this._inputType === InputType.TEXT
            || this._inputType === InputType.PASSWORD
            || this._inputType === InputType.DATE
            || this._inputType === InputType.DATETIME
            || this._inputType === InputType.EMAIL
            || this._inputType === InputType.JSON
            || this._inputType === InputType.MONTH
            || this._inputType === InputType.TEXTAREA
            || this._inputType === InputType.TIME
            || this._inputType === InputType.URL
            || this._inputType === InputType.WEEK;
    }

    protected inputTypeSupportsHelperText() {
        return this.inputTypeSupportsButton()
            || this._inputType === InputType.SELECT;
    }

    /**
     * This is called by asset-mixin
     */
    public _onEvent(event: SharedEvent) {
        if (event.eventType !== "attribute") {
            return;
        }

        const oldValue = this.getValue();
        this._attributeEvent = event as AttributeEvent;
        this._onAttributeValueChanged(oldValue, this._attributeEvent.attributeState!.value, event.timestamp);
    }

    protected _onAttributeValueChanged(oldValue: any, newValue: any, timestamp?: number) {
        if (this.attribute) {
            this.attribute.value = newValue;
            this.attribute.valueTimestamp = timestamp;
        }

        this._clearWriteTimeout();
        this.value = newValue;
        this._sendError = false;
        this.dispatchEvent(new OrAttributeInputChangedEvent(newValue, oldValue));
    }

    protected _updateValue(newValue: any) {
        const oldValue = this.getValue();

        if (this.readonly || this.isReadonly()) {
            return;
        }

        if (this._writeTimeoutHandler) {
            return;
        }

        // If we have an attributeRef then send an update and wait for the updated attribute event to come back through
        // the system or for the attribute property to be updated by a parent control or timeout and reset the value
        const attributeRef = this._getAttributeRef();

        if (attributeRef && !this.disableWrite) {

            super._sendEvent({
                eventType: "attribute",
                attributeState: {
                    attributeRef: attributeRef,
                    value: newValue
                }
            } as AttributeEvent);

            this._writeTimeoutHandler = window.setTimeout(() => this._onWriteTimeout(), this.writeTimeout);
        } else {
            this.value = newValue;
            this.dispatchEvent(new OrAttributeInputChangedEvent(newValue, oldValue));
        }
    }

    protected _clearWriteTimeout() {
        if (this._writeTimeoutHandler) {
            window.clearTimeout(this._writeTimeoutHandler);
        }
        this._writeTimeoutHandler = undefined;
    }

    protected _onWriteTimeout() {
        this._sendError = true;
        if (!this.inputTypeSupportsButton()) {
            // Put the old value back
            this._attrInput.value = this.getValue();
        }
        if (this.hasHelperText) {
            this.requestUpdate();
        }
        this._clearWriteTimeout();
    }
}
